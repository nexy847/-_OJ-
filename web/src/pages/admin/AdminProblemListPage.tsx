import { useQuery } from '@tanstack/react-query'
import type { TableProps } from 'antd'
import { Button, Card, Space, Table, Tag, Typography } from 'antd'
import { useNavigate } from 'react-router-dom'
import { listProblems } from '../../api/problems'
import { ProblemResponse } from '../../types/api'
import { formatDateTime } from '../../utils/format'

export function AdminProblemListPage() {
  const navigate = useNavigate()
  const { data = [], isLoading } = useQuery({
    queryKey: ['admin', 'problems'],
    queryFn: listProblems,
  })

  const columns: TableProps<ProblemResponse>['columns'] = [
    { title: 'ID', dataIndex: 'id', width: 80 },
    {
      title: 'Problem',
      dataIndex: 'title',
      render: (_: unknown, record) => (
        <Space direction="vertical" size={0}>
          <Typography.Text strong>{record.title}</Typography.Text>
          <Typography.Text type="secondary">{record.description}</Typography.Text>
        </Space>
      ),
    },
    {
      title: 'Limits',
      render: (_: unknown, record) => (
        <Space>
          <Tag color="blue">{record.timeLimitMs} ms</Tag>
          <Tag color="purple">{record.memoryLimitMb} MB</Tag>
        </Space>
      ),
    },
    {
      title: 'Created At',
      dataIndex: 'createdAt',
      render: (value: string) => formatDateTime(value),
    },
    {
      title: 'Action',
      render: (_: unknown, record) => (
        <Space>
          <Button type="link" onClick={() => navigate(`/problems/${record.id}`)}>
            View
          </Button>
          <Button type="link" onClick={() => navigate(`/admin/problems/${record.id}`)}>
            Manage
          </Button>
        </Space>
      ),
    },
  ]

  return (
    <Card>
      <div className="page-toolbar">
        <div>
          <Typography.Title level={3} style={{ margin: 0 }}>
            Problem Administration
          </Typography.Title>
          <Typography.Paragraph type="secondary" style={{ marginBottom: 0 }}>
            Review all problems and open testcase management for any item.
          </Typography.Paragraph>
        </div>
        <Button type="primary" onClick={() => navigate('/admin/problems/create')}>
          Create Problem
        </Button>
      </div>
      <Table rowKey="id" loading={isLoading} columns={columns} dataSource={data} />
    </Card>
  )
}
