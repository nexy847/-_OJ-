import {
  BarChartOutlined,
  CloudUploadOutlined,
  FileAddOutlined,
  FileTextOutlined,
  ProfileOutlined,
  TeamOutlined,
  LineChartOutlined,
  LogoutOutlined,
  OrderedListOutlined,
  RiseOutlined,
  RadarChartOutlined,
  SettingOutlined,
} from '@ant-design/icons'
import { Button, Layout, Menu, Space, Typography } from 'antd'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../app/useAuth'

const { Header, Sider, Content } = Layout

export function AppLayout() {
  const navigate = useNavigate()
  const location = useLocation()
  const { user, isAdmin, logout } = useAuth()

  const items = [
    ...(!isAdmin ? [{ key: '/problems', icon: <OrderedListOutlined />, label: 'Problems' }] : []),
    { key: '/analytics/user', icon: <LineChartOutlined />, label: 'My Analytics' },
    { key: '/profile', icon: <SettingOutlined />, label: 'Profile' },
    ...(isAdmin
      ? [
          { key: '/admin/problems', icon: <FileTextOutlined />, label: 'Problem Admin' },
          { key: '/admin/problems/create', icon: <FileAddOutlined />, label: 'Create Problem' },
          { key: '/admin/users/create', icon: <TeamOutlined />, label: 'Create User' },
          { key: '/admin/submissions', icon: <ProfileOutlined />, label: 'All Submissions' },
          { key: '/admin/problems/difficulty', icon: <RiseOutlined />, label: 'Problem Difficulty' },
          { key: '/admin/analytics', icon: <BarChartOutlined />, label: 'System Analytics' },
          { key: '/admin/perdict', icon: <RadarChartOutlined />, label: 'Perdict' },
          { key: '/admin/export', icon: <CloudUploadOutlined />, label: 'Export HDFS' },
        ]
      : []),
  ]

  const selectedKey =
    items.find((item) => location.pathname === item.key || location.pathname.startsWith(`${item.key}/`))?.key ??
    (isAdmin ? '/admin/problems' : '/problems')

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider breakpoint="lg" collapsedWidth="0">
        <div style={{ padding: '20px 16px', color: '#fff', fontSize: 18, fontWeight: 700 }}>OJ Frontend</div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[selectedKey]}
          items={items}
          onClick={({ key }) => navigate(key)}
        />
      </Sider>
      <Layout>
        <Header
          style={{
            background: '#fff',
            padding: '0 24px',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
          }}
        >
          <Typography.Title level={4} style={{ margin: 0 }}>
            Online Judge
          </Typography.Title>
          <Space>
            <Typography.Text>
              {user?.username} / {isAdmin ? 'Admin' : 'User'}
            </Typography.Text>
            <Button
              icon={<LogoutOutlined />}
              onClick={() => {
                logout()
                navigate('/login', { replace: true })
              }}
            >
              Logout
            </Button>
          </Space>
        </Header>
        <Content style={{ padding: 24 }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}
