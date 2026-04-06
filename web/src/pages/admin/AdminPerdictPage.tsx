import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import ReactECharts from 'echarts-for-react'
import dayjs, { Dayjs } from 'dayjs'
import type { TableProps } from 'antd'
import { Card, DatePicker, Empty, Space, Statistic, Table, Typography } from 'antd'
import { getAdminSubmissionForecast } from '../../api/analytics'
import { AdminSubmissionForecastResponse } from '../../types/api'
import { formatNumber } from '../../utils/format'

const chartStyle = { height: 360 }

export function AdminPerdictPage() {
  const [forecastDate, setForecastDate] = useState<string | undefined>(undefined)

  const forecastQuery = useQuery({
    queryKey: ['analytics', 'admin', 'perdict', forecastDate ?? 'latest'],
    queryFn: () => getAdminSubmissionForecast(forecastDate),
  })

  const rows = forecastQuery.data ?? []
  const latestForecastDate = rows[0]?.forecastDate
  const totalPredicted = rows.reduce((sum, row) => sum + row.predictedSubmissions, 0)
  const maxPredicted = rows.reduce((max, row) => Math.max(max, row.predictedSubmissions), 0)
  const avgPredicted = rows.length === 0 ? 0 : totalPredicted / rows.length
  const modelName = rows[0]?.modelName ?? '-'

  const columns: TableProps<AdminSubmissionForecastResponse>['columns'] = [
    { title: 'Forecast Date', dataIndex: 'forecastDate' },
    { title: 'Target Date', dataIndex: 'targetDate' },
    { title: 'Predicted Submissions', dataIndex: 'predictedSubmissions' },
    { title: 'Model', dataIndex: 'modelName' },
  ]

  return (
    <Space direction="vertical" size={16} style={{ display: 'flex' }}>
      <Card>
        <div className="page-toolbar">
          <div>
            <Typography.Title level={3} style={{ margin: 0 }}>
              Perdict
            </Typography.Title>
            <Typography.Paragraph type="secondary" style={{ marginBottom: 0 }}>
              Review future submission volume predicted by the Spark forecasting job.
            </Typography.Paragraph>
          </div>
          <DatePicker
            value={forecastDate ? dayjs(forecastDate) : null}
            onChange={(value: Dayjs | null) => setForecastDate(value ? value.format('YYYY-MM-DD') : undefined)}
            allowClear
            placeholder="Latest batch"
          />
        </div>
      </Card>

      <Space size={16} wrap>
        <Card loading={forecastQuery.isLoading}>
          <Statistic title="Forecast Batch" value={latestForecastDate ?? '-'} />
        </Card>
        <Card loading={forecastQuery.isLoading}>
          <Statistic title="Predicted Days" value={rows.length} />
        </Card>
        <Card loading={forecastQuery.isLoading}>
          <Statistic title="Total Predicted" value={totalPredicted} />
        </Card>
        <Card loading={forecastQuery.isLoading}>
          <Statistic title="Average Predicted" value={formatNumber(avgPredicted)} />
        </Card>
        <Card loading={forecastQuery.isLoading}>
          <Statistic title="Peak Predicted" value={maxPredicted} />
        </Card>
      </Space>

      <Card title={`Submission Forecast${latestForecastDate ? ` (${latestForecastDate})` : ''}`} loading={forecastQuery.isLoading}>
        {rows.length === 0 ? (
          <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="No forecast data" />
        ) : (
          <ReactECharts
            style={chartStyle}
            option={{
              tooltip: { trigger: 'axis' },
              xAxis: { type: 'category', data: rows.map((item) => item.targetDate) },
              yAxis: { type: 'value', name: 'Submissions' },
              series: [
                {
                  name: 'Predicted Submissions',
                  type: 'line',
                  smooth: true,
                  areaStyle: {},
                  data: rows.map((item) => item.predictedSubmissions),
                },
              ],
            }}
          />
        )}
      </Card>

      <Card title={`Forecast Details${modelName !== '-' ? ` - ${modelName}` : ''}`}>
        <Table
          rowKey={(record) => `${record.forecastDate}-${record.targetDate}-${record.modelName}`}
          loading={forecastQuery.isLoading}
          columns={columns}
          dataSource={rows}
          pagination={false}
          locale={{ emptyText: 'No forecast rows' }}
        />
      </Card>
    </Space>
  )
}
