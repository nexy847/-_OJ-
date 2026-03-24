import { useMemo, useState } from 'react'
import { useMutation, useQuery } from '@tanstack/react-query'
import type { TableProps } from 'antd'
import {
  Button,
  Card,
  Col,
  Empty,
  Form,
  Input,
  Row,
  Select,
  Space,
  Statistic,
  Table,
  Tag,
  Typography,
  message,
} from 'antd'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { getUserRecent } from '../api/analytics'
import { extractApiError } from '../api/client'
import { getProblem } from '../api/problems'
import { createSubmission } from '../api/submissions'
import { CreateSubmissionPayload, Language, UserRecentSubmissionResponse } from '../types/api'
import { formatDateTime } from '../utils/format'

const languageOptions: { label: string; value: Language }[] = [
  { label: 'C', value: 'C' },
  { label: 'C++', value: 'CPP' },
  { label: 'Java', value: 'JAVA' },
  { label: 'Python', value: 'PYTHON' },
]

const codeTemplates: Record<Language, string> = {
  C: '#include <stdio.h>\n\nint main(void) {\n    return 0;\n}\n',
  CPP: '#include <bits/stdc++.h>\nusing namespace std;\n\nint main() {\n    return 0;\n}\n',
  JAVA:
    'import java.util.*;\n\npublic class Main {\n    public static void main(String[] args) {\n    }\n}\n',
  PYTHON: 'def main():\n    pass\n\n\nif __name__ == "__main__":\n    main()\n',
}

type SubmissionForm = {
  language: Language
  code: string
}

export function ProblemDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [form] = Form.useForm<SubmissionForm>()
  const [selectedLanguage, setSelectedLanguage] = useState<Language>('CPP')

  const problemQuery = useQuery({
    queryKey: ['problem', id],
    queryFn: () => getProblem(id!),
    enabled: Boolean(id),
  })

  const recentQuery = useQuery({
    queryKey: ['analytics', 'user', 'recent', 20],
    queryFn: () => getUserRecent(20),
  })

  const submissionMutation = useMutation({
    mutationFn: (payload: CreateSubmissionPayload) => createSubmission(payload),
  })

  const problem = problemQuery.data
  const recentRows = useMemo(
    () => (recentQuery.data ?? []).filter((item) => item.problemId === Number(id)),
    [id, recentQuery.data],
  )

  const onFinish = async (values: SubmissionForm) => {
    if (!problem) {
      return
    }
    try {
      const created = await submissionMutation.mutateAsync({
        problemId: problem.id,
        language: values.language,
        code: values.code,
      })
      message.success('Submission created')
      navigate(`/submissions/${created.id}`)
    } catch (error) {
      message.error(extractApiError(error))
    }
  }

  const recentColumns: TableProps<UserRecentSubmissionResponse>['columns'] = [
    {
      title: 'Submission ID',
      dataIndex: 'submissionId',
      render: (value: number) => <Link to={`/submissions/${value}`}>{value}</Link>,
    },
    {
      title: 'Language',
      dataIndex: 'language',
      render: (value: string) => <Tag>{value}</Tag>,
    },
    {
      title: 'Verdict',
      dataIndex: 'verdict',
      render: (value: string) => <Tag color={value === 'AC' ? 'green' : 'orange'}>{value}</Tag>,
    },
    {
      title: 'Time',
      dataIndex: 'timeMs',
      render: (value: number) => `${value ?? 0} ms`,
    },
    {
      title: 'Memory',
      dataIndex: 'memoryKb',
      render: (value: number) => `${value ?? 0} KB`,
    },
    {
      title: 'Created At',
      dataIndex: 'createdAt',
      render: (value: string) => formatDateTime(value),
    },
  ]

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
              {problem.title}
            </Typography.Title>
            <Typography.Paragraph type="secondary" style={{ marginBottom: 0 }}>
              Problem #{problem.id}
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

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={16}>
          <Card title="Submit Code">
            <Form<SubmissionForm>
              form={form}
              layout="vertical"
              initialValues={{ language: selectedLanguage, code: codeTemplates[selectedLanguage] }}
              onFinish={onFinish}
            >
              <Form.Item
                name="language"
                label="Language"
                rules={[{ required: true, message: 'Please select a language' }]}
              >
                <Select
                  options={languageOptions}
                  onChange={(value: Language) => {
                    setSelectedLanguage(value)
                    form.setFieldsValue({ code: codeTemplates[value] })
                  }}
                />
              </Form.Item>
              <Form.Item
                name="code"
                label="Code"
                rules={[{ required: true, message: 'Please enter code' }]}
              >
                <Input.TextArea
                  className="monospace-textarea"
                  autoSize={{ minRows: 16, maxRows: 24 }}
                  placeholder="Paste or type your solution here"
                />
              </Form.Item>
              <Space>
                <Button type="primary" htmlType="submit" loading={submissionMutation.isPending}>
                  Submit
                </Button>
                <Button
                  onClick={() => {
                    form.setFieldsValue({ code: codeTemplates[selectedLanguage] })
                  }}
                >
                  Reset Template
                </Button>
              </Space>
            </Form>
          </Card>
        </Col>
        <Col xs={24} lg={8}>
          <Space direction="vertical" size={16} style={{ display: 'flex' }}>
            <Card title="Problem Meta">
              <Row gutter={[16, 16]}>
                <Col span={12}>
                  <Statistic title="Time Limit" value={problem.timeLimitMs} suffix="ms" />
                </Col>
                <Col span={12}>
                  <Statistic title="Memory Limit" value={problem.memoryLimitMb} suffix="MB" />
                </Col>
              </Row>
            </Card>
            <Card title="Note">
              <Typography.Paragraph style={{ marginBottom: 0 }}>
                Testcases are maintained in the admin management pages. This page only shows the statement and accepts
                code submissions.
              </Typography.Paragraph>
            </Card>
          </Space>
        </Col>
      </Row>

      <Card title="Recent Submissions For This Problem">
        <Table
          rowKey="submissionId"
          loading={recentQuery.isLoading}
          columns={recentColumns}
          dataSource={recentRows}
          pagination={false}
          locale={{ emptyText: 'No recent submission' }}
        />
      </Card>
    </Space>
  )
}
