import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import ReactECharts from 'echarts-for-react'
import dayjs, { Dayjs } from 'dayjs'
import type { TableProps } from 'antd'
import {
  Card,
  Col,
  DatePicker,
  Empty,
  Row,
  Select,
  Space,
  Statistic,
  Table,
  Typography,
} from 'antd'
import {
  getAdminDaily,
  getAdminLanguage,
  getAdminOverview,
  getAdminTopProblems,
  getAdminTopUsers,
  getAdminVerdict,
} from '../../api/analytics'
import { AdminProblemStatResponse, AdminUserStatResponse } from '../../types/api'
import { formatNumber, formatPercent } from '../../utils/format'

const chartStyle = { height: 320 }

export function AdminAnalyticsPage() {
  const [date, setDate] = useState(dayjs().format('YYYY-MM-DD'))
  const [days, setDays] = useState(30)

  const overviewQuery = useQuery({
    queryKey: ['analytics', 'admin', 'overview', date],
    queryFn: () => getAdminOverview(date),
  })
  const dailyQuery = useQuery({
    queryKey: ['analytics', 'admin', 'daily', days],
    queryFn: () => getAdminDaily(days),
  })
  const languageQuery = useQuery({
    queryKey: ['analytics', 'admin', 'language', date],
    queryFn: () => getAdminLanguage(date),
  })
  const verdictQuery = useQuery({
    queryKey: ['analytics', 'admin', 'verdict', date],
    queryFn: () => getAdminVerdict(date),
  })
  const topProblemQuery = useQuery({
    queryKey: ['analytics', 'admin', 'problems', date, 20],
    queryFn: () => getAdminTopProblems(date, 20),
  })
  const topUserQuery = useQuery({
    queryKey: ['analytics', 'admin', 'users', date, 20],
    queryFn: () => getAdminTopUsers(date, 20),
  })

  const overview = overviewQuery.data
  const daily = dailyQuery.data ?? []
  const language = languageQuery.data ?? []
  const verdict = verdictQuery.data ?? []
  const topProblems = topProblemQuery.data ?? []
  const topUsers = topUserQuery.data ?? []

  const problemColumns: TableProps<AdminProblemStatResponse>['columns'] = [
    { title: 'Problem ID', dataIndex: 'problemId' },
    { title: 'Problem Title', dataIndex: 'problemTitle', render: (value: string | null) => value ?? '-' },
    { title: 'Submissions', dataIndex: 'total' },
    { title: 'Accepted', dataIndex: 'accepted' },
    { title: 'AC Rate', dataIndex: 'acRate', render: (value: number) => formatPercent(value) },
    { title: 'Avg Time', dataIndex: 'avgTimeMs', render: (value: number) => `${formatNumber(value)} ms` },
    { title: 'Avg Memory', dataIndex: 'avgMemoryKb', render: (value: number) => `${formatNumber(value)} KB` },
  ]

  const userColumns: TableProps<AdminUserStatResponse>['columns'] = [
    { title: 'User ID', dataIndex: 'userId' },
    { title: 'Username', dataIndex: 'username', render: (value: string | null) => value ?? '-' },
    { title: 'Submissions', dataIndex: 'total' },
    { title: 'Accepted', dataIndex: 'accepted' },
    { title: 'AC Rate', dataIndex: 'acRate', render: (value: number) => formatPercent(value) },
  ]

  return (
    <Space direction="vertical" size={16} style={{ display: 'flex' }}>
      <Card>
        <div className="page-toolbar">
          <div>
            <Typography.Title level={3} style={{ margin: 0 }}>
              System Analytics
            </Typography.Title>
            <Typography.Paragraph type="secondary" style={{ marginBottom: 0 }}>
              Review platform trends, language mix, verdict distribution, top problems, and active users.
            </Typography.Paragraph>
          </div>
          <Space wrap>
            <DatePicker
              value={dayjs(date)}
              onChange={(value: Dayjs | null) => setDate((value ?? dayjs()).format('YYYY-MM-DD'))}
              allowClear={false}
            />
            <Select
              value={days}
              style={{ width: 140 }}
              options={[
                { label: 'Last 7 days', value: 7 },
                { label: 'Last 30 days', value: 30 },
                { label: 'Last 90 days', value: 90 },
              ]}
              onChange={setDays}
            />
          </Space>
        </div>
      </Card>

      <Row gutter={[16, 16]}>
        <Col xs={24} md={12} xl={4}>
          <Card loading={overviewQuery.isLoading}>
            <Statistic title="Daily Submissions" value={overview?.total ?? 0} />
          </Card>
        </Col>
        <Col xs={24} md={12} xl={4}>
          <Card loading={overviewQuery.isLoading}>
            <Statistic title="Daily Accepted" value={overview?.accepted ?? 0} />
          </Card>
        </Col>
        <Col xs={24} md={12} xl={4}>
          <Card loading={overviewQuery.isLoading}>
            <Statistic title="Daily AC Rate" value={formatPercent(overview?.acRate ?? 0)} />
          </Card>
        </Col>
        <Col xs={24} md={12} xl={4}>
          <Card loading={overviewQuery.isLoading}>
            <Statistic title="Average Time" value={formatNumber(overview?.avgTimeMs ?? 0)} suffix="ms" />
          </Card>
        </Col>
        <Col xs={24} md={12} xl={4}>
          <Card loading={overviewQuery.isLoading}>
            <Statistic title="Average Memory" value={formatNumber(overview?.avgMemoryKb ?? 0)} suffix="KB" />
          </Card>
        </Col>
        <Col xs={24} md={12} xl={4}>
          <Card loading={overviewQuery.isLoading}>
            <Statistic title="Active Users" value={overview?.activeUsers ?? 0} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} xl={14}>
          <Card title={`${days}-Day Trend`} loading={dailyQuery.isLoading}>
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
          <Card title={`${days}-Day AC Rate`} loading={dailyQuery.isLoading}>
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
                      areaStyle: {},
                      data: daily.map((item) => Number((item.acRate * 100).toFixed(2))),
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
          <Card title={`Language Distribution (${date})`} loading={languageQuery.isLoading}>
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
          <Card title={`Verdict Distribution (${date})`} loading={verdictQuery.isLoading}>
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

      <Row gutter={[16, 16]}>
        <Col xs={24} xl={12}>
          <Card title={`Top Problems (${date})`}>
            <Table
              rowKey="problemId"
              loading={topProblemQuery.isLoading}
              columns={problemColumns}
              dataSource={topProblems}
              pagination={{ pageSize: 10 }}
              locale={{ emptyText: 'No problem stats' }}
            />
          </Card>
        </Col>
        <Col xs={24} xl={12}>
          <Card title={`Top Users (${date})`}>
            <Table
              rowKey="userId"
              loading={topUserQuery.isLoading}
              columns={userColumns}
              dataSource={topUsers}
              pagination={{ pageSize: 10 }}
              locale={{ emptyText: 'No user stats' }}
            />
          </Card>
        </Col>
      </Row>
    </Space>
  )
}
