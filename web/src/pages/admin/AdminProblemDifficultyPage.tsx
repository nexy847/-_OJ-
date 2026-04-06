import { useEffect, useMemo, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import ReactECharts from 'echarts-for-react'
import dayjs, { Dayjs } from 'dayjs'
import type { TableProps } from 'antd'
import { Card, Col, DatePicker, Empty, Row, Select, Space, Statistic, Table, Typography } from 'antd'
import { getAdminProblemDifficulty, getAdminProblemDifficultyHistory } from '../../api/analytics'
import { AdminProblemDifficultyResponse } from '../../types/api'
import { formatNumber, formatPercent } from '../../utils/format'

const chartStyle = { height: 320 }

export function AdminProblemDifficultyPage() {
  const [date, setDate] = useState(dayjs().format('YYYY-MM-DD'))
  const [label, setLabel] = useState<string | undefined>(undefined)
  const [selectedProblemId, setSelectedProblemId] = useState<number | undefined>(undefined)

  const difficultyQuery = useQuery({
    queryKey: ['analytics', 'admin', 'problem-difficulty', date, label ?? 'ALL'],
    queryFn: () => getAdminProblemDifficulty(date, 100, label),
  })

  const rows = difficultyQuery.data ?? []

  useEffect(() => {
    if (!selectedProblemId || !rows.some((row) => row.problemId === selectedProblemId)) {
      setSelectedProblemId(rows[0]?.problemId)
    }
  }, [rows, selectedProblemId])

  const historyQuery = useQuery({
    queryKey: ['analytics', 'admin', 'problem-difficulty-history', selectedProblemId ?? 0],
    queryFn: () => getAdminProblemDifficultyHistory(selectedProblemId!, 30),
    enabled: !!selectedProblemId,
  })

  const historyRows = historyQuery.data ?? []
  const hardCount = rows.filter((row) => row.difficultyLabel === 'Hard').length
  const mediumCount = rows.filter((row) => row.difficultyLabel === 'Medium').length
  const easyCount = rows.filter((row) => row.difficultyLabel === 'Easy').length
  const avgScore = rows.length === 0 ? 0 : rows.reduce((sum, row) => sum + row.difficultyScore, 0) / rows.length

  const topHardRows = useMemo(() => rows.slice(0, 10), [rows])

  const columns: TableProps<AdminProblemDifficultyResponse>['columns'] = [
    { title: 'Problem ID', dataIndex: 'problemId' },
    { title: 'Problem Title', dataIndex: 'problemTitle', render: (value: string | null) => value ?? '-' },
    { title: 'Label', dataIndex: 'difficultyLabel' },
    { title: 'Score', dataIndex: 'difficultyScore', render: (value: number) => formatNumber(value) },
    { title: 'AC Rate', dataIndex: 'acRate', render: (value: number) => formatPercent(value) },
    { title: 'Avg Time', dataIndex: 'avgTimeMs', render: (value: number) => `${formatNumber(value)} ms` },
    { title: 'Avg Attempts', dataIndex: 'avgAttemptsPerUser', render: (value: number) => formatNumber(value) },
    { title: 'WA Rate', dataIndex: 'waRate', render: (value: number) => formatPercent(value) },
    { title: 'RE Rate', dataIndex: 'reRate', render: (value: number) => formatPercent(value) },
    { title: 'TLE Rate', dataIndex: 'tleRate', render: (value: number) => formatPercent(value) },
  ]

  return (
    <Space direction="vertical" size={16} style={{ display: 'flex' }}>
      <Card>
        <div className="page-toolbar">
          <div>
            <Typography.Title level={3} style={{ margin: 0 }}>
              Problem Difficulty
            </Typography.Title>
            <Typography.Paragraph type="secondary" style={{ marginBottom: 0 }}>
              Review rule-based difficulty scores generated from Spark over historical judge behavior.
            </Typography.Paragraph>
          </div>
          <Space wrap>
            <DatePicker
              value={dayjs(date)}
              onChange={(value: Dayjs | null) => setDate((value ?? dayjs()).format('YYYY-MM-DD'))}
              allowClear={false}
            />
            <Select
              value={label}
              style={{ width: 160 }}
              allowClear
              placeholder="All labels"
              options={[
                { label: 'Easy', value: 'Easy' },
                { label: 'Medium', value: 'Medium' },
                { label: 'Hard', value: 'Hard' },
              ]}
              onChange={(value) => setLabel(value)}
            />
            <Select
              value={selectedProblemId}
              style={{ width: 260 }}
              placeholder="Select problem"
              options={rows.map((row) => ({
                label: `#${row.problemId} ${row.problemTitle ?? ''}`.trim(),
                value: row.problemId,
              }))}
              onChange={(value) => setSelectedProblemId(value)}
            />
          </Space>
        </div>
      </Card>

      <Row gutter={[16, 16]}>
        <Col xs={24} md={12} xl={6}>
          <Card loading={difficultyQuery.isLoading}>
            <Statistic title="Tracked Problems" value={rows.length} />
          </Card>
        </Col>
        <Col xs={24} md={12} xl={6}>
          <Card loading={difficultyQuery.isLoading}>
            <Statistic title="Hard Problems" value={hardCount} />
          </Card>
        </Col>
        <Col xs={24} md={12} xl={6}>
          <Card loading={difficultyQuery.isLoading}>
            <Statistic title="Medium Problems" value={mediumCount} />
          </Card>
        </Col>
        <Col xs={24} md={12} xl={6}>
          <Card loading={difficultyQuery.isLoading}>
            <Statistic title="Average Difficulty" value={formatNumber(avgScore)} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} xl={12}>
          <Card title={`Difficulty Label Distribution (${date})`} loading={difficultyQuery.isLoading}>
            {rows.length === 0 ? (
              <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="No difficulty data" />
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
                      data: [
                        { name: 'Easy', value: easyCount },
                        { name: 'Medium', value: mediumCount },
                        { name: 'Hard', value: hardCount },
                      ],
                    },
                  ],
                }}
              />
            )}
          </Card>
        </Col>
        <Col xs={24} xl={12}>
          <Card title={`Top Hard Problems (${date})`} loading={difficultyQuery.isLoading}>
            {topHardRows.length === 0 ? (
              <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="No difficulty ranking" />
            ) : (
              <ReactECharts
                style={chartStyle}
                option={{
                  tooltip: { trigger: 'axis' },
                  xAxis: {
                    type: 'value',
                    max: 1,
                  },
                  yAxis: {
                    type: 'category',
                    data: topHardRows.map((row) => `#${row.problemId}`),
                    inverse: true,
                  },
                  series: [
                    {
                      type: 'bar',
                      data: topHardRows.map((row) => Number(row.difficultyScore.toFixed(4))),
                    },
                  ],
                }}
              />
            )}
          </Card>
        </Col>
      </Row>

      <Card
        title={selectedProblemId ? `Difficulty History (#${selectedProblemId})` : 'Difficulty History'}
        loading={historyQuery.isLoading}
      >
        {historyRows.length === 0 ? (
          <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="No history data" />
        ) : (
          <ReactECharts
            style={chartStyle}
            option={{
              tooltip: { trigger: 'axis' },
              legend: { data: ['Difficulty Score', 'AC Rate', 'Avg Attempts'] },
              xAxis: { type: 'category', data: historyRows.map((item) => item.date) },
              yAxis: [
                { type: 'value', name: 'Score / Rate', max: 1 },
                { type: 'value', name: 'Attempts' },
              ],
              series: [
                { name: 'Difficulty Score', type: 'line', smooth: true, data: historyRows.map((item) => item.difficultyScore) },
                { name: 'AC Rate', type: 'line', smooth: true, data: historyRows.map((item) => item.acRate) },
                { name: 'Avg Attempts', type: 'bar', yAxisIndex: 1, data: historyRows.map((item) => item.avgAttemptsPerUser) },
              ],
            }}
          />
        )}
      </Card>

      <Card title={`Problem Difficulty Details (${date})`}>
        <Table
          rowKey={(record) => `${record.date}-${record.problemId}`}
          loading={difficultyQuery.isLoading}
          columns={columns}
          dataSource={rows}
          pagination={{ pageSize: 10 }}
          locale={{ emptyText: 'No difficulty rows' }}
        />
      </Card>
    </Space>
  )
}
