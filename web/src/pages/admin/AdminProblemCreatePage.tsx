import { useMutation, useQueryClient } from '@tanstack/react-query'
import { Button, Card, Form, Input, InputNumber, Space, Typography, message } from 'antd'
import { useNavigate } from 'react-router-dom'
import { extractApiError } from '../../api/client'
import { createProblem } from '../../api/problems'

type ProblemForm = {
  title: string
  description: string
  timeLimitMs: number
  memoryLimitMb: number
  testcaseContents?: {
    inputContent: string
    outputContent: string
    weight: number
  }[]
}

export function AdminProblemCreatePage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const mutation = useMutation({
    mutationFn: createProblem,
    onSuccess: async (created) => {
      await queryClient.invalidateQueries({ queryKey: ['problems'] })
      await queryClient.invalidateQueries({ queryKey: ['admin', 'problems'] })
      message.success('Problem created')
      navigate(`/admin/problems/${created.id}`, { replace: true })
    },
  })

  const onFinish = async (values: ProblemForm) => {
    try {
      await mutation.mutateAsync({
        ...values,
        testcases: [],
        testcaseContents: (values.testcaseContents ?? []).filter(
          (item) => item.inputContent?.trim() && item.outputContent?.trim(),
        ),
      })
    } catch (error) {
      message.error(extractApiError(error))
    }
  }

  return (
    <Card>
      <div className="page-toolbar">
        <div>
          <Typography.Title level={3} style={{ margin: 0 }}>
            Create Problem
          </Typography.Title>
          <Typography.Paragraph type="secondary" style={{ marginBottom: 0 }}>
            Create the problem first, then add testcases from the detail page.
          </Typography.Paragraph>
        </div>
      </div>
      <Form<ProblemForm>
        layout="vertical"
        style={{ maxWidth: 720 }}
        initialValues={{ timeLimitMs: 1000, memoryLimitMb: 256 }}
        onFinish={onFinish}
      >
        <Form.Item name="title" label="Title" rules={[{ required: true, message: 'Please enter title' }]}>
          <Input maxLength={128} placeholder="Example: A + B" />
        </Form.Item>
        <Form.Item
          name="description"
          label="Description"
          rules={[{ required: true, message: 'Please enter description' }]}
        >
          <Input.TextArea autoSize={{ minRows: 6, maxRows: 12 }} maxLength={4000} />
        </Form.Item>
        <Space size={16} align="start" wrap>
          <Form.Item
            name="timeLimitMs"
            label="Time Limit (ms)"
            rules={[{ required: true, message: 'Please enter time limit' }]}
          >
            <InputNumber min={1} max={60000} />
          </Form.Item>
          <Form.Item
            name="memoryLimitMb"
            label="Memory Limit (MB)"
            rules={[{ required: true, message: 'Please enter memory limit' }]}
          >
            <InputNumber min={16} max={2048} />
          </Form.Item>
        </Space>
        <Form.List name="testcaseContents">
          {(fields, { add, remove }) => (
            <Space direction="vertical" size={16} style={{ display: 'flex', marginBottom: 16 }}>
              <Typography.Title level={5} style={{ margin: 0 }}>
                Initial Testcases
              </Typography.Title>
              <Typography.Paragraph type="secondary" style={{ marginBottom: 0 }}>
                Optional. You can create the problem first and add testcases later, or attach them here directly.
              </Typography.Paragraph>
              {fields.map((field, index) => (
                <Card
                  key={field.key}
                  type="inner"
                  title={`Testcase ${index + 1}`}
                  extra={
                    <Button danger type="link" onClick={() => remove(field.name)}>
                      Remove
                    </Button>
                  }
                >
                  <Form.Item
                    {...field}
                    name={[field.name, 'inputContent']}
                    label="Input Content"
                    rules={[{ required: true, message: 'Please enter input content' }]}
                  >
                    <Input.TextArea autoSize={{ minRows: 4, maxRows: 8 }} className="monospace-textarea" />
                  </Form.Item>
                  <Form.Item
                    {...field}
                    name={[field.name, 'outputContent']}
                    label="Expected Output"
                    rules={[{ required: true, message: 'Please enter expected output' }]}
                  >
                    <Input.TextArea autoSize={{ minRows: 4, maxRows: 8 }} className="monospace-textarea" />
                  </Form.Item>
                  <Form.Item
                    {...field}
                    name={[field.name, 'weight']}
                    label="Weight"
                    initialValue={1}
                    rules={[{ required: true, message: 'Please enter weight' }]}
                  >
                    <InputNumber min={1} max={1000} />
                  </Form.Item>
                </Card>
              ))}
              <Button onClick={() => add({ inputContent: '', outputContent: '', weight: 1 })}>
                Add Testcase
              </Button>
            </Space>
          )}
        </Form.List>
        <Space>
          <Button type="primary" htmlType="submit" loading={mutation.isPending}>
            Create
          </Button>
          <Button onClick={() => navigate('/admin/problems')}>Back</Button>
        </Space>
      </Form>
    </Card>
  )
}
