import { useQuery } from '@tanstack/react-query'
import { Alert, Card, Descriptions, Empty, Space, Tag, Typography } from 'antd'
import { Link, useParams } from 'react-router-dom'
import { getSubmission, getSubmissionResultOrNull } from '../api/submissions'
import { formatDateTime } from '../utils/format'

const activeStatuses = new Set(['PENDING', 'RUNNING'])

export function SubmissionDetailPage() {
  const { id } = useParams<{ id: string }>()

  const submissionQuery = useQuery({
    queryKey: ['submission', id],
    queryFn: () => getSubmission(id!),
    enabled: Boolean(id),
    refetchInterval: (query) => {
      const status = query.state.data?.status
      return status && activeStatuses.has(status) ? 2000 : false
    },
  })

  const resultQuery = useQuery({
    queryKey: ['submission-result', id],
    queryFn: () => getSubmissionResultOrNull(id!),
    enabled: Boolean(id),
    refetchInterval: () => {
      const status = submissionQuery.data?.status
      return status && activeStatuses.has(status) ? 2000 : false
    },
  })

  if (submissionQuery.isLoading) {
    return <Card loading />
  }

  const submission = submissionQuery.data

  if (!submission) {
    return (
      <Card>
        <Empty description="Submission not found" />
      </Card>
    )
  }

  const result = resultQuery.data

  return (
    <Space direction="vertical" size={16} style={{ display: 'flex' }}>
      <Card>
        <div className="page-toolbar">
          <div>
            <Typography.Title level={3} style={{ margin: 0 }}>
              Submission #{submission.id}
            </Typography.Title>
            <Typography.Paragraph type="secondary" style={{ marginBottom: 0 }}>
              <Link to={`/problems/${submission.problemId}`}>Back to problem #{submission.problemId}</Link>
            </Typography.Paragraph>
          </div>
          <Space>
            <Tag>{submission.language}</Tag>
            <Tag color={submission.verdict === 'AC' ? 'green' : activeStatuses.has(submission.status) ? 'blue' : 'orange'}>
              {submission.verdict}
            </Tag>
          </Space>
        </div>
        <Descriptions bordered column={2}>
          <Descriptions.Item label="Status">{submission.status}</Descriptions.Item>
          <Descriptions.Item label="Verdict">{submission.verdict}</Descriptions.Item>
          <Descriptions.Item label="User ID">{submission.userId}</Descriptions.Item>
          <Descriptions.Item label="Problem ID">{submission.problemId}</Descriptions.Item>
          <Descriptions.Item label="Created At">{formatDateTime(submission.createdAt)}</Descriptions.Item>
          <Descriptions.Item label="Updated At">{formatDateTime(submission.updatedAt)}</Descriptions.Item>
        </Descriptions>
      </Card>

      <Card title="Judge Result">
        {result ? (
          <Space direction="vertical" size={16} style={{ display: 'flex' }}>
            <Descriptions bordered column={2}>
              <Descriptions.Item label="Verdict">{result.verdict}</Descriptions.Item>
              <Descriptions.Item label="Runtime">{result.timeMs} ms</Descriptions.Item>
              <Descriptions.Item label="Memory">{result.memoryKb} KB</Descriptions.Item>
              <Descriptions.Item label="Created At">{formatDateTime(result.createdAt)}</Descriptions.Item>
            </Descriptions>

            {result.message ? <Alert type="info" showIcon message={result.message} /> : null}

            {result.compileError ? (
              <Card type="inner" title="Compile Error">
                <pre className="monospace-block" style={{ margin: 0, whiteSpace: 'pre-wrap' }}>
                  {result.compileError}
                </pre>
              </Card>
            ) : null}

            {result.runtimeError ? (
              <Card type="inner" title="Runtime Error">
                <pre className="monospace-block" style={{ margin: 0, whiteSpace: 'pre-wrap' }}>
                  {result.runtimeError}
                </pre>
              </Card>
            ) : null}
          </Space>
        ) : (
          <Alert
            type="info"
            showIcon
            message={activeStatuses.has(submission.status) ? 'Judging in progress. This page refreshes automatically.' : 'Result is not available yet.'}
          />
        )}
      </Card>
    </Space>
  )
}
