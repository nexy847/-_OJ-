import { useMutation } from '@tanstack/react-query'
import { Button, Card, Form, Input, Space, Typography, message } from 'antd'
import { useNavigate } from 'react-router-dom'
import { createUserByAdmin } from '../../api/auth'
import { extractApiError } from '../../api/client'

type AdminUserForm = {
  username: string
  password: string
}

export function AdminUserCreatePage() {
  const navigate = useNavigate()
  const [form] = Form.useForm<AdminUserForm>()

  const mutation = useMutation({
    mutationFn: createUserByAdmin,
    onSuccess: (created) => {
      message.success(`User created: ${created.username}`)
      form.resetFields()
    },
  })

  const onFinish = async (values: AdminUserForm) => {
    try {
      await mutation.mutateAsync(values)
    } catch (error) {
      message.error(extractApiError(error))
    }
  }

  return (
    <Card>
      <div className="page-toolbar">
        <div>
          <Typography.Title level={3} style={{ margin: 0 }}>
            Create User
          </Typography.Title>
          <Typography.Paragraph type="secondary" style={{ marginBottom: 0 }}>
            Administrators can directly create a normal user account with username and password.
          </Typography.Paragraph>
        </div>
      </div>
      <Form<AdminUserForm>
        form={form}
        layout="vertical"
        style={{ maxWidth: 560 }}
        onFinish={onFinish}
      >
        <Form.Item
          name="username"
          label="Username"
          rules={[
            { required: true, message: 'Please enter username' },
            { max: 64, message: 'Username is too long' },
          ]}
        >
          <Input maxLength={64} placeholder="Example: student01" />
        </Form.Item>
        <Form.Item
          name="password"
          label="Password"
          rules={[
            { required: true, message: 'Please enter password' },
            { max: 128, message: 'Password is too long' },
          ]}
        >
          <Input.Password maxLength={128} placeholder="Initial password" />
        </Form.Item>
        <Space>
          <Button type="primary" htmlType="submit" loading={mutation.isPending}>
            Create User
          </Button>
          <Button onClick={() => navigate('/admin/problems')}>Back</Button>
        </Space>
      </Form>
    </Card>
  )
}
