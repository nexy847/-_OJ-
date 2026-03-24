import { Flex, Spin } from 'antd'

export function LoadingScreen() {
  return (
    <Flex style={{ minHeight: '100vh' }} align="center" justify="center">
      <Spin size="large" />
    </Flex>
  )
}
