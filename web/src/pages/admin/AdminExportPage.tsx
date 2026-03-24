import { useMutation } from '@tanstack/react-query'
import { Alert, Button, Card, Space, Typography, message } from 'antd'
import { extractApiError } from '../../api/client'
import { exportHdfs } from '../../api/admin'

export function AdminExportPage() {
  const mutation = useMutation({
    mutationFn: exportHdfs,
    onSuccess: (result) => {
      message.success('Export finished')
      message.info(result)
    },
  })

  const onExport = async () => {
    try {
      await mutation.mutateAsync()
    } catch (error) {
      message.error(extractApiError(error))
    }
  }

  return (
    <Card>
      <Space direction="vertical" size={16} style={{ display: 'flex' }}>
        <div>
          <Typography.Title level={3} style={{ margin: 0 }}>
            Export To HDFS
          </Typography.Title>
          <Typography.Paragraph type="secondary" style={{ marginBottom: 0 }}>
            Trigger the backend to export pending analysis events to HDFS.
          </Typography.Paragraph>
        </div>

        <Alert
          type="info"
          showIcon
          message="This action only exports raw events. Daily summary generation still needs agg_daily.sh on Hadoop."
        />

        <Space>
          <Button type="primary" onClick={onExport} loading={mutation.isPending}>
            Export Now
          </Button>
          {mutation.data ? <Typography.Text code>{mutation.data}</Typography.Text> : null}
        </Space>
      </Space>
    </Card>
  )
}
