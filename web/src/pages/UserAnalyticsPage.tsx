import { useQuery } from '@tanstack/react-query'
import ReactECharts from 'echarts-for-react'
import type { TableProps } from 'antd'
import { Card, Col, Empty, Row, Space, Statistic, Table, Tag, Typography } from 'antd'
import {
  getUserDaily,
  getUserLanguage,
  getUserOverview,
  getUserProblems,
  getUserRecent,
  getUserVerdict,
} from '../api/analytics'
import { UserProblemStatResponse, UserRecentSubmissionResponse } from '../types/api'
import { formatDateTime, formatNumber, formatPercent } from '../utils/format'

const chartStyle = { height: 320 }

export function UserAnalyticsPage() {
  const overviewQuery = useQuery({
    queryKey: ['analytics', 'user', 'overview'],
    queryFn: getUserOverview,
  })
  const dailyQuery = useQuery({
    queryKey: ['analytics', 'user', 'daily', 30],
    queryFn: () => getUserDaily(30),
  })
  const languageQuery = useQuery({
    queryKey: ['analytics', 'user', 'language'],
    queryFn: getUserLanguage,
  })
  const verdictQuery = useQuery({
    queryKey: ['analytics', 'user', 'verdict'],
    queryFn: getUserVerdict,
  })
  const problemQuery = useQuery({
    queryKey: ['analytics', 'user', 'problems', 50],
    queryFn: () => getUserProblems(50),
  })
  const recentQuery = useQuery({
    queryKey: ['analytics', 'user', 'recent', 10],
    queryFn: () => getUserRecent(10),
  })

  const overview = overviewQuery.data
  const daily = dailyQuery.data ?? []
  const language = languageQuery.data ?? []
  const verdict = verdictQuery.data ?? []
  const problems = problemQuery.data ?? []
  const recent = recentQuery.data ?? []

  const problemColumns: TableProps<UserProblemStatResponse>['columns'] = [
    { title: 'Problem ID', dataIndex: 'problemId' },
    { title: 'Problem Title', dataIndex: 'problemTitle', render: (value: string | null) => value ?? '-' },
    { title: 'Submissions', dataIndex: 'total' },
    { title: 'Accepted', dataIndex: 'accepted' },
    { title: 'AC Rate', dataIndex: 'acRate', render: (value: number) => formatPercent(value) },
    { title: 'Avg Time', dataIndex: 'avgTimeMs', render: (value: number) => `${formatNumber(value)} ms` },
    { title: 'Avg Memory', dataIndex: 'avgMemoryKb', render: (value: number) => `${formatNumber(value)} KB` },
  ]

  const recentColumns: TableProps<UserRecentSubmissionResponse>['columns'] = [
    { title: 'Submission ID', dataIndex: 'submissionId' },
    { title: 'Problem', dataIndex: 'problemTitle', render: (value: string | null, record) => value ?? `#${record.problemId}` },
    { title: 'Language', dataIndex: 'language', render: (value: string) => <Tag>{value}</Tag> },
    {
      title: 'Verdict',
      dataIndex: 'verdict',
      render: (value: string) => <Tag color={value === 'AC' ? 'green' : 'orange'}>{value}</Tag>,
    },
    { title: 'Time', dataIndex: 'timeMs', render: (value: number) => `${value} ms` },
    { title: 'Memory', dataIndex: 'memoryKb', render: (value: number) => `${value} KB` },
    { title: 'Created At', dataIndex: 'createdAt', render: (value: string) => formatDateTime(value) },
  ]

  return (
    <Space direction="vertical" size={16} style={{ display: 'flex' }}>
      <Card>
        <Typography.Title level={3} style={{ margin: 0 }}>
          My Analytics
        </Typography.Title>
        <Typography.Paragraph type="secondary" style={{ marginBottom: 0 }}>
          This page shows your submission volume, acceptance rate, language usage, and recent submission history.
        </Typography.Paragraph>
      </Card>

      <Row gutter={[16, 16]}>
        <Col xs={24} md={12} xl={4}>
          <Card loading={overviewQuery.isLoading}>
            <Statistic title="Total Submissions" value={overview?.total ?? 0} />
          </Card>
        </Col>
        <Col xs={24} md={12} xl={4}>
          <Card loading={overviewQuery.isLoading}>
            <Statistic title="Accepted" value={overview?.accepted ?? 0} />
          </Card>
        </Col>
        <Col xs={24} md={12} xl={4}>
          <Card loading={overviewQuery.isLoading}>
            <Statistic title="AC Rate" value={formatPercent(overview?.acRate ?? 0)} />
          </Card>
        </Col>
        <Col xs={24} md={12} xl={6}>
          <Card loading={overviewQuery.isLoading}>
            <Statistic title="Average Time" value={formatNumber(overview?.avgTimeMs ?? 0)} suffix="ms" />
          </Card>
        </Col>
        <Col xs={24} md={12} xl={6}>
          <Card loading={overviewQuery.isLoading}>
            <Statistic title="Average Memory" value={formatNumber(overview?.avgMemoryKb ?? 0)} suffix="KB" />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} xl={14}>
          <Card title="30-Day Submission Trend" loading={dailyQuery.isLoading}>
            {daily.length === 0 ? (
              <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="No trend data" />
            ) : (
              <ReactECharts
                style={chartStyle}
                option={{
                  tooltip: { trigger: 'axis' },
                  legend: { data: ['Submissions', 'Accepted'] },
                  xAxis: { type: 'category', data: daily.map((item) => item.date) },
                  yAxis: { type: 'value' },
                  series: [
                    { name: 'Submissions', type: 'bar', data: daily.map((item) => item.total) },
                    { name: 'Accepted', type: 'line', smooth: true, data: daily.map((item) => item.accepted) },
                  ],
                }}
              />
            )}
          </Card>
        </Col>
        <Col xs={24} xl={10}>
          <Card title="30-Day AC Rate" loading={dailyQuery.isLoading}>
            {daily.length === 0 ? (
              <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="No AC rate data" />
            ) : (
              <ReactECharts
                style={chartStyle}
                option={{
                  tooltip: { trigger: 'axis' },
                  xAxis: { type: 'category', data: daily.map((item) => item.date) },
                  yAxis: { type: 'value', axisLabel: { formatter: '{value}%' } },
                  series: [
                    {
                      name: 'AC Rate',
                      type: 'line',
                      smooth: true,
                      data: daily.map((item) => Number((item.acRate * 100).toFixed(2))),
                      areaStyle: {},
                    },
                  ],
                }}
              />
            )}
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} xl={12}>
          <Card title="Language Distribution" loading={languageQuery.isLoading}>
            {language.length === 0 ? (
              <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="No language data" />
            ) : (
              <ReactECharts
                style={chartStyle}
                option={{
                  tooltip: { trigger: 'item' },
                  legend: { bottom: 0 },
                  series: [
                    {
                      type: 'pie',
                      radius: ['35%', '65%'],
                      data: language.map((item) => ({
                        name: `${item.language} (${formatPercent(item.acRate)})`,
                        value: item.total,
                      })),
                    },
                  ],
                }}
              />
            )}
          </Card>
        </Col>
        <Col xs={24} xl={12}>
          <Card title="Verdict Distribution" loading={verdictQuery.isLoading}>
            {verdict.length === 0 ? (
              <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="No verdict data" />
            ) : (
              <ReactECharts
                style={chartStyle}
                option={{
                  tooltip: { trigger: 'item' },
                  legend: { bottom: 0 },
                  series: [
                    {
                      type: 'pie',
                      radius: ['35%', '65%'],
                      data: verdict.map((item) => ({
                        name: item.verdict,
                        value: item.total,
                      })),
                    },
                  ],
                }}
              />
            )}
          </Card>
        </Col>
      </Row>

      <Card title="Problem Stats">
        <Table
          rowKey="problemId"
          loading={problemQuery.isLoading}
          columns={problemColumns}
          dataSource={problems}
          pagination={{ pageSize: 10 }}
          locale={{ emptyText: 'No problem stats' }}
        />
      </Card>

      <Card title="Recent Submissions">
        <Table
          rowKey="submissionId"
          loading={recentQuery.isLoading}
          columns={recentColumns}
          dataSource={recent}
          pagination={false}
          locale={{ emptyText: 'No recent submission' }}
        />
      </Card>
    </Space>
  )
}
