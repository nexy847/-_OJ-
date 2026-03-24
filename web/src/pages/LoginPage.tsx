import { LockOutlined, UserOutlined } from '@ant-design/icons'
import { useMutation } from '@tanstack/react-query'
import { Button, Card, Form, Input, Typography, message } from 'antd'
import { Link, useNavigate } from 'react-router-dom'
import { login } from '../api/auth'
import { extractApiError } from '../api/client'
import { useAuth } from '../app/useAuth'

type LoginForm = {
  username: string
  password: string
}

export function LoginPage() {
  const navigate = useNavigate()
  const auth = useAuth()
  const mutation = useMutation({
    mutationFn: (values: LoginForm) => login(values.username, values.password),
  })

  const onFinish = async (values: LoginForm) => {
    try {
      const response = await mutation.mutateAsync(values)
      const me = await auth.login(response.token)
      message.success('Login success')
      navigate(me.role === 'ADMIN' ? '/admin/analytics' : '/problems', { replace: true })
    } catch (error) {
      message.error(extractApiError(error))
    }
  }

  return (
    <div style={{ minHeight: '100vh', display: 'grid', placeItems: 'center', padding: 24 }}>
      <Card style={{ width: 420 }}>
        <Typography.Title level={3} style={{ marginTop: 0 }}>
          Login
        </Typography.Title>
        <Typography.Paragraph type="secondary">
          Sign in with an existing account. The app will restore your session and permissions automatically.
        </Typography.Paragraph>
        <Form<LoginForm> layout="vertical" onFinish={onFinish}>
          <Form.Item name="username" label="Username" rules={[{ required: true, message: 'Please enter username' }]}>
            <Input prefix={<UserOutlined />} placeholder="Enter username" />
          </Form.Item>
          <Form.Item name="password" label="Password" rules={[{ required: true, message: 'Please enter password' }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="Enter password" />
          </Form.Item>
          <Button type="primary" htmlType="submit" block loading={mutation.isPending}>
            Login
          </Button>
        </Form>
        <Typography.Paragraph style={{ marginBottom: 0, marginTop: 16 }}>
          No account yet? <Link to="/register">Register</Link>
        </Typography.Paragraph>
      </Card>
    </div>
  )
}
