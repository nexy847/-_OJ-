import { useEffect, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import type { TableProps } from 'antd'
import {
  Alert,
  Button,
  Card,
  Col,
  Empty,
  Form,
  Input,
  InputNumber,
  Modal,
  Popconfirm,
  Row,
  Space,
  Table,
  Tag,
  Typography,
  message,
} from 'antd'
import { Link, useParams } from 'react-router-dom'
import { extractApiError } from '../../api/client'
import {
  createProblemTestcase,
  deleteProblemTestcase,
  getProblem,
  listProblemTestcases,
  updateProblem,
  updateProblemTestcase,
} from '../../api/problems'
import { CreateTestcasePayload, TestcaseResponse } from '../../types/api'

type TestcaseForm = {
  inputContent: string
  outputContent: string
  weight: number
}

type ProblemMetaForm = {
  title: string
  description: string
  timeLimitMs: number
  memoryLimitMb: number
}

export function AdminProblemDetailPage() {
  const { id } = useParams<{ id: string }>()
  const queryClient = useQueryClient()
  const [metaForm] = Form.useForm<ProblemMetaForm>()
  const [createForm] = Form.useForm<TestcaseForm>()
  const [editForm] = Form.useForm<TestcaseForm>()
  const [editing, setEditing] = useState<TestcaseResponse | null>(null)

  const problemQuery = useQuery({
    queryKey: ['problem', id],
    queryFn: () => getProblem(id!),
    enabled: Boolean(id),
  })
  const testcaseQuery = useQuery({
    queryKey: ['problem-testcases', id],
    queryFn: () => listProblemTestcases(id!),
    enabled: Boolean(id),
  })

  const refresh = async () => {
    await queryClient.invalidateQueries({ queryKey: ['problem', id] })
    await queryClient.invalidateQueries({ queryKey: ['problem-testcases', id] })
    await queryClient.invalidateQueries({ queryKey: ['problems'] })
    await queryClient.invalidateQueries({ queryKey: ['admin', 'problems'] })
  }

  const createMutation = useMutation({
    mutationFn: (payload: CreateTestcasePayload) => createProblemTestcase(id!, payload),
    onSuccess: async () => {
      createForm.resetFields()
      await refresh()
      message.success('Testcase created')
    },
  })

  const updateProblemMutation = useMutation({
    mutationFn: (values: ProblemMetaForm) => updateProblem(id!, values),
    onSuccess: async () => {
      await refresh()
      message.success('Problem metadata updated')
    },
  })

  const updateMutation = useMutation({
    mutationFn: (payload: { testcaseId: number; data: CreateTestcasePayload }) =>
      updateProblemTestcase(id!, payload.testcaseId, payload.data),
    onSuccess: async () => {
      setEditing(null)
      editForm.resetFields()
      await refresh()
      message.success('Testcase overwritten')
    },
  })

  const deleteMutation = useMutation({
    mutationFn: (testcaseId: number) => deleteProblemTestcase(id!, testcaseId),
    onSuccess: async () => {
      await refresh()
      message.success('Testcase deleted')
    },
  })

  const onCreate = async (values: TestcaseForm) => {
    try {
      await createMutation.mutateAsync(values)
    } catch (error) {
      message.error(extractApiError(error))
    }
  }

  const onUpdate = async (values: TestcaseForm) => {
    if (!editing) {
      return
    }
    try {
      await updateMutation.mutateAsync({
        testcaseId: editing.id,
        data: values,
      })
    } catch (error) {
      message.error(extractApiError(error))
    }
  }

  const onUpdateProblem = async (values: ProblemMetaForm) => {
    try {
      await updateProblemMutation.mutateAsync(values)
    } catch (error) {
      message.error(extractApiError(error))
    }
  }

  const columns: TableProps<TestcaseResponse>['columns'] = [
    { title: 'ID', dataIndex: 'id', width: 80 },
    { title: 'Input Path', dataIndex: 'inputPath' },
    { title: 'Output Path', dataIndex: 'outputPath' },
    { title: 'Weight', dataIndex: 'weight', width: 100 },
    {
      title: 'Action',
      render: (_: unknown, record) => (
        <Space>
          <Button
            type="link"
            onClick={() => {
              setEditing(record)
              editForm.setFieldsValue({
                inputContent: '',
                outputContent: '',
                weight: record.weight,
              })
            }}
          >
            Overwrite
          </Button>
          <Popconfirm
            title="Delete this testcase?"
            onConfirm={() =>
              deleteMutation.mutate(record.id, {
                onError: (error) => message.error(extractApiError(error)),
              })
            }
          >
            <Button type="link" danger loading={deleteMutation.isPending}>
              Delete
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ]

  const problem = problemQuery.data

  useEffect(() => {
    if (problem) {
      metaForm.setFieldsValue({
        title: problem.title,
        description: problem.description,
        timeLimitMs: problem.timeLimitMs,
        memoryLimitMb: problem.memoryLimitMb,
      })
    }
  }, [metaForm, problem])

  if (problemQuery.isLoading) {
    return <Card loading />
  }

  if (!problem) {
    return (
      <Card>
        <Empty description="Problem not found" />
      </Card>
    )
  }

  return (
    <Space direction="vertical" size={16} style={{ display: 'flex' }}>
      <Card>
        <div className="page-toolbar">
          <div>
            <Typography.Title level={3} style={{ margin: 0 }}>
              Manage Problem: {problem.title}
            </Typography.Title>
            <Typography.Paragraph type="secondary" style={{ marginBottom: 0 }}>
              <Link to={`/problems/${problem.id}`}>Open the user-facing problem page</Link>
            </Typography.Paragraph>
          </div>
          <Space>
            <Tag color="blue">{problem.timeLimitMs} ms</Tag>
            <Tag color="purple">{problem.memoryLimitMb} MB</Tag>
          </Space>
        </div>
        <Typography.Paragraph style={{ whiteSpace: 'pre-wrap', marginBottom: 0 }}>
          {problem.description}
        </Typography.Paragraph>
      </Card>

      <Card title="Edit Problem Metadata">
        <Form<ProblemMetaForm> form={metaForm} layout="vertical" onFinish={onUpdateProblem}>
          <Form.Item name="title" label="Title" rules={[{ required: true, message: 'Please enter title' }]}>
            <Input maxLength={128} />
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
          <Space>
            <Button type="primary" htmlType="submit" loading={updateProblemMutation.isPending}>
              Save Metadata
            </Button>
            <Button
              onClick={() =>
                metaForm.setFieldsValue({
                  title: problem.title,
                  description: problem.description,
                  timeLimitMs: problem.timeLimitMs,
                  memoryLimitMb: problem.memoryLimitMb,
                })
              }
            >
              Reset
            </Button>
          </Space>
        </Form>
      </Card>

      <Row gutter={[16, 16]}>
        <Col xs={24} xl={10}>
          <Card title="Add Testcase">
            <Form<TestcaseForm>
              form={createForm}
              layout="vertical"
              initialValues={{ weight: 1 }}
              onFinish={onCreate}
            >
              <Form.Item
                name="inputContent"
                label="Input Content"
                rules={[{ required: true, message: 'Please enter input content' }]}
              >
                <Input.TextArea autoSize={{ minRows: 8, maxRows: 12 }} className="monospace-textarea" />
              </Form.Item>
              <Form.Item
                name="outputContent"
                label="Expected Output"
                rules={[{ required: true, message: 'Please enter expected output' }]}
              >
                <Input.TextArea autoSize={{ minRows: 8, maxRows: 12 }} className="monospace-textarea" />
              </Form.Item>
              <Form.Item name="weight" label="Weight" rules={[{ required: true, message: 'Please enter weight' }]}>
                <InputNumber min={1} max={1000} />
              </Form.Item>
              <Button type="primary" htmlType="submit" loading={createMutation.isPending}>
                Add Testcase
              </Button>
            </Form>
          </Card>
        </Col>
        <Col xs={24} xl={14}>
          <Card
            title="Testcase List"
            extra={
              <Button onClick={() => testcaseQuery.refetch()} loading={testcaseQuery.isFetching}>
                Refresh
              </Button>
            }
          >
            <Table
              rowKey="id"
              loading={testcaseQuery.isLoading}
              columns={columns}
              dataSource={testcaseQuery.data ?? []}
              pagination={false}
              locale={{ emptyText: 'No testcase' }}
            />
          </Card>
        </Col>
      </Row>

      <Modal
        open={Boolean(editing)}
        title={editing ? `Overwrite Testcase #${editing.id}` : 'Overwrite Testcase'}
        onCancel={() => {
          setEditing(null)
          editForm.resetFields()
        }}
        onOk={() => editForm.submit()}
        confirmLoading={updateMutation.isPending}
        destroyOnClose
      >
        <Alert
          type="warning"
          showIcon
          style={{ marginBottom: 16 }}
          message="The current backend only returns file paths, not testcase content. Overwrite requires full new input and output."
        />
        <Form<TestcaseForm> form={editForm} layout="vertical" onFinish={onUpdate}>
          <Form.Item
            name="inputContent"
            label="Input Content"
            rules={[{ required: true, message: 'Please enter input content' }]}
          >
            <Input.TextArea autoSize={{ minRows: 6, maxRows: 10 }} className="monospace-textarea" />
          </Form.Item>
          <Form.Item
            name="outputContent"
            label="Expected Output"
            rules={[{ required: true, message: 'Please enter expected output' }]}
          >
            <Input.TextArea autoSize={{ minRows: 6, maxRows: 10 }} className="monospace-textarea" />
          </Form.Item>
          <Form.Item name="weight" label="Weight" rules={[{ required: true, message: 'Please enter weight' }]}>
            <InputNumber min={1} max={1000} />
          </Form.Item>
        </Form>
      </Modal>
    </Space>
  )
}
