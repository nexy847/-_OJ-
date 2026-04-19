import { useQuery } from '@tanstack/react-query'
import type { TableProps } from 'antd'
import { Button, Card, Space, Table, Tag, Typography } from 'antd'
import { useNavigate } from 'react-router-dom'
import { listProblems } from '../api/problems'
import { useAuth } from '../app/useAuth'
import { ProblemResponse } from '../types/api'
import { formatDateTime } from '../utils/format'

function difficultyColor(label: string) {
  if (label === 'Easy') return 'green'
  if (label === 'Medium') return 'orange'
  if (label === 'Hard') return 'red'
  return 'default'
}

export function ProblemListPage() {
  const navigate = useNavigate()
  const { isAdmin } = useAuth()
  const { data = [], isLoading } = useQuery({
    queryKey: ['problems'],
    queryFn: listProblems,
  })

  const columns: TableProps<ProblemResponse>['columns'] = [
    {
      title: 'Problem',
      dataIndex: 'title',
      render: (_: unknown, record) => (
        <Typography.Link onClick={() => navigate(`/problems/${record.id}`)}>{record.title}</Typography.Link>
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
      title: 'Difficulty',
      dataIndex: 'difficultyLabel',
      render: (value: string) => <Tag color={difficultyColor(value)}>{value}</Tag>,
    },
    {
      title: 'Created At',
      dataIndex: 'createdAt',
      render: (value: string) => formatDateTime(value),
    },
    {
      title: 'Action',
      render: (_: unknown, record) => (
        <Button type="link" onClick={() => navigate(`/problems/${record.id}`)}>
          View
        </Button>
      ),
    },
  ]

  return (
    <Card>
      <div className="page-toolbar">
        <div>
          <Typography.Title level={3} style={{ margin: 0 }}>
            Problems
          </Typography.Title>
          <Typography.Paragraph type="secondary" style={{ marginBottom: 0 }}>
            Browse a problem, read the statement, and submit your solution.
          </Typography.Paragraph>
        </div>
        {isAdmin ? (
          <Button type="primary" onClick={() => navigate('/admin/problems/create')}>
            Create Problem
          </Button>
        ) : null}
      </div>
      <Table rowKey="id" loading={isLoading} columns={columns} dataSource={data} />
    </Card>
  )
}
