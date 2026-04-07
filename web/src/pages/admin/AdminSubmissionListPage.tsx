import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import type { TableProps } from 'antd'
import { Button, Card, Descriptions, Drawer, Empty, Space, Table, Tag, Typography, Alert } from 'antd'
import { getAdminSubmissionDetail, getAdminSubmissions } from '../../api/admin-submissions'
import { AdminSubmissionListItemResponse } from '../../types/api'
import { formatDateTime } from '../../utils/format'

function verdictColor(verdict?: string | null) {
  if (verdict === 'AC') return 'green'
  if (verdict === 'PENDING' || verdict === 'RUNNING') return 'blue'
  if (verdict === 'CE' || verdict === 'RE' || verdict === 'TLE' || verdict === 'MLE' || verdict === 'WA') return 'orange'
  return 'default'
}

export function AdminSubmissionListPage() {
  const [selectedId, setSelectedId] = useState<number | null>(null)

  const listQuery = useQuery({
    queryKey: ['admin', 'submissions'],
    queryFn: getAdminSubmissions,
  })

  const detailQuery = useQuery({
    queryKey: ['admin', 'submission-detail', selectedId],
    queryFn: () => getAdminSubmissionDetail(selectedId!),
    enabled: selectedId !== null,
  })

  const rows = listQuery.data ?? []
  const detail = detailQuery.data

  const columns: TableProps<AdminSubmissionListItemResponse>['columns'] = [
    { title: 'Username', dataIndex: 'username', render: (value: string | null) => value ?? '-' },
    { title: 'Problem Title', dataIndex: 'problemTitle', render: (value: string | null) => value ?? '-' },
    {
      title: 'Verdict',
      dataIndex: 'verdict',
      render: (value: string | null) => <Tag color={verdictColor(value)}>{value ?? '-'}</Tag>,
    },
    { title: 'Created At', dataIndex: 'createdAt', render: (value: string) => formatDateTime(value) },
    {
      title: 'Action',
      key: 'action',
      render: (_, record) => (
        <Button type="link" onClick={() => setSelectedId(record.submissionId)}>
          View
        </Button>
      ),
    },
  ]

  return (
    <>
      <Card>
        <div className="page-toolbar">
          <div>
            <Typography.Title level={3} style={{ margin: 0 }}>
              All Submissions
            </Typography.Title>
            <Typography.Paragraph type="secondary" style={{ marginBottom: 0 }}>
              Review every submission in chronological order and inspect full judge details, including source code.
            </Typography.Paragraph>
          </div>
        </div>
        <Table
          rowKey="submissionId"
          loading={listQuery.isLoading}
          columns={columns}
          dataSource={rows}
          pagination={{ pageSize: 10 }}
          locale={{ emptyText: 'No submissions' }}
        />
      </Card>

      <Drawer
        title={selectedId ? `Submission #${selectedId}` : 'Submission Detail'}
        width={920}
        open={selectedId !== null}
        onClose={() => setSelectedId(null)}
        destroyOnClose
      >
        {detailQuery.isLoading ? (
          <Card loading />
        ) : detail ? (
          <Space direction="vertical" size={16} style={{ display: 'flex' }}>
            <Descriptions bordered column={2}>
              <Descriptions.Item label="Username">{detail.username}</Descriptions.Item>
              <Descriptions.Item label="User ID">{detail.userId}</Descriptions.Item>
              <Descriptions.Item label="Problem">{detail.problemTitle}</Descriptions.Item>
              <Descriptions.Item label="Problem ID">{detail.problemId}</Descriptions.Item>
              <Descriptions.Item label="Language">{detail.language}</Descriptions.Item>
              <Descriptions.Item label="Status">{detail.status}</Descriptions.Item>
              <Descriptions.Item label="Verdict">
                <Tag color={verdictColor(detail.verdict)}>{detail.verdict}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="Created At">{formatDateTime(detail.createdAt)}</Descriptions.Item>
              <Descriptions.Item label="Updated At">{formatDateTime(detail.updatedAt)}</Descriptions.Item>
              <Descriptions.Item label="Runtime">{detail.timeMs ?? '-'} ms</Descriptions.Item>
              <Descriptions.Item label="Memory">{detail.memoryKb ?? '-'} KB</Descriptions.Item>
              <Descriptions.Item label="Model Message" span={2}>{detail.message ?? '-'}</Descriptions.Item>
            </Descriptions>

            <Card type="inner" title="Source Code">
              <pre className="monospace-block" style={{ margin: 0, whiteSpace: 'pre-wrap' }}>
                {detail.code}
              </pre>
            </Card>

            {detail.compileError ? (
              <Card type="inner" title="Compile Error">
                <pre className="monospace-block" style={{ margin: 0, whiteSpace: 'pre-wrap' }}>
                  {detail.compileError}
                </pre>
              </Card>
            ) : null}

            {detail.runtimeError ? (
              <Card type="inner" title="Runtime Error">
                <pre className="monospace-block" style={{ margin: 0, whiteSpace: 'pre-wrap' }}>
                  {detail.runtimeError}
                </pre>
              </Card>
            ) : null}

            {!detail.compileError && !detail.runtimeError && !detail.message ? (
              <Alert type="info" showIcon message="No extra judge diagnostics" />
            ) : null}
          </Space>
        ) : (
          <Empty description="Submission detail not found" />
        )}
      </Drawer>
    </>
  )
}
