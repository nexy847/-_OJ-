import { useEffect } from 'react'
import { useMutation } from '@tanstack/react-query'
import { Button, Card, Form, Input, Space, Typography, message } from 'antd'
import { useNavigate } from 'react-router-dom'
import { updateCurrentUser } from '../api/auth'
import { extractApiError } from '../api/client'
import { useAuth } from '../app/useAuth'

type ProfileForm = {
  username: string
  currentPassword: string
  newPassword?: string
  confirmNewPassword?: string
}

export function ProfilePage() {
  const [form] = Form.useForm<ProfileForm>()
  const navigate = useNavigate()
  const auth = useAuth()
  const mutation = useMutation({
    mutationFn: updateCurrentUser,
  })

  useEffect(() => {
    if (auth.user) {
      form.setFieldsValue({
        username: auth.user.username,
        currentPassword: '',
        newPassword: '',
        confirmNewPassword: '',
      })
    }
  }, [auth.user, form])

  const onFinish = async (values: ProfileForm) => {
    try {
      await mutation.mutateAsync({
        username: values.username,
        currentPassword: values.currentPassword,
        newPassword: values.newPassword?.trim() ? values.newPassword : undefined,
      })
      auth.logout()
      message.success('Profile updated. Please login again.')
      navigate('/login', { replace: true })
    } catch (error) {
      message.error(extractApiError(error))
    }
  }

  return (
    <Card>
      <div className="page-toolbar">
        <div>
          <Typography.Title level={3} style={{ margin: 0 }}>
            Profile
          </Typography.Title>
          <Typography.Paragraph type="secondary" style={{ marginBottom: 0 }}>
            Update your username or password. After saving, the current session will be cleared and you need to login
            again.
          </Typography.Paragraph>
        </div>
      </div>

      <Form<ProfileForm> form={form} layout="vertical" style={{ maxWidth: 560 }} onFinish={onFinish}>
        <Form.Item
          name="username"
          label="Username"
          rules={[{ required: true, message: 'Please enter username' }]}
        >
          <Input maxLength={64} />
        </Form.Item>
        <Form.Item
          name="currentPassword"
          label="Current Password"
          rules={[{ required: true, message: 'Please enter current password' }]}
        >
          <Input.Password />
        </Form.Item>
        <Form.Item
          name="newPassword"
          label="New Password"
          rules={[
            {
              validator(_, value) {
                if (!value || value.length >= 6) {
                  return Promise.resolve()
                }
                return Promise.reject(new Error('New password must be at least 6 characters'))
              },
            },
          ]}
        >
          <Input.Password placeholder="Leave blank to keep current password" />
        </Form.Item>
        <Form.Item
          name="confirmNewPassword"
          label="Confirm New Password"
          dependencies={['newPassword']}
          rules={[
            ({ getFieldValue }) => ({
              validator(_, value) {
                const newPassword = getFieldValue('newPassword')
                if (!newPassword && !value) {
                  return Promise.resolve()
                }
                if (value === newPassword) {
                  return Promise.resolve()
                }
                return Promise.reject(new Error('The new passwords do not match'))
              },
            }),
          ]}
        >
          <Input.Password placeholder="Repeat the new password if you entered one" />
        </Form.Item>
        <Space>
          <Button type="primary" htmlType="submit" loading={mutation.isPending}>
            Save Changes
          </Button>
          <Button onClick={() => form.resetFields()}>Reset</Button>
        </Space>
      </Form>
    </Card>
  )
}
