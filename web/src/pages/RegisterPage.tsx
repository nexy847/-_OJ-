import { LockOutlined, UserOutlined } from '@ant-design/icons'
import { useMutation } from '@tanstack/react-query'
import { Button, Card, Form, Input, Typography, message } from 'antd'
import { Link, useNavigate } from 'react-router-dom'
import { register } from '../api/auth'
import { extractApiError } from '../api/client'

type RegisterForm = {
  username: string
  password: string
  confirmPassword: string
}

export function RegisterPage() {
  const navigate = useNavigate()
  const mutation = useMutation({
    mutationFn: (values: RegisterForm) =>
      register({
        username: values.username,
        password: values.password,
      }),
  })

  const onFinish = async (values: RegisterForm) => {
    try {
      await mutation.mutateAsync(values)
      message.success('Register success, please login')
      navigate('/login', { replace: true })
    } catch (error) {
      message.error(extractApiError(error))
    }
  }

  return (
    <div style={{ minHeight: '100vh', display: 'grid', placeItems: 'center', padding: 24 }}>
      <Card style={{ width: 420 }}>
        <Typography.Title level={3} style={{ marginTop: 0 }}>
          Register
        </Typography.Title>
        <Typography.Paragraph type="secondary">
          Create a new account, then sign in and start solving problems.
        </Typography.Paragraph>
        <Form<RegisterForm> layout="vertical" onFinish={onFinish}>
          <Form.Item name="username" label="Username" rules={[{ required: true, message: 'Please enter username' }]}>
            <Input prefix={<UserOutlined />} placeholder="Enter username" />
          </Form.Item>
          <Form.Item name="password" label="Password" rules={[{ required: true, message: 'Please enter password' }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="Enter password" />
          </Form.Item>
          <Form.Item
            name="confirmPassword"
            label="Confirm Password"
            dependencies={['password']}
            rules={[
              { required: true, message: 'Please confirm password' },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('password') === value) {
                    return Promise.resolve()
                  }
                  return Promise.reject(new Error('Passwords do not match'))
                },
              }),
            ]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="Enter password again" />
          </Form.Item>
          <Button type="primary" htmlType="submit" block loading={mutation.isPending}>
            Register
          </Button>
        </Form>
        <Typography.Paragraph style={{ marginBottom: 0, marginTop: 16 }}>
          Already have an account? <Link to="/login">Back to login</Link>
        </Typography.Paragraph>
      </Card>
    </div>
  )
}
