import { Button, Result } from 'antd'
import { useNavigate } from 'react-router-dom'

export function NotFoundPage() {
  const navigate = useNavigate()
  return (
    <Result
      status="404"
      title="Page Not Found"
      subTitle="The page you requested does not exist or the route is invalid."
      extra={
        <Button type="primary" onClick={() => navigate('/problems')}>
          Back To Problems
        </Button>
      }
    />
  )
}
