import "./App.css";
import Graph, { GraphProps } from "./components/Graph";
import {
  AutoComplete,
  Button,
  Col,
  Input,
  Layout,
  Row,
  Slider,
  Switch,
} from "antd";
import { Header, Content } from "antd/lib/layout/layout";
import React, { FC } from "react";
import { CaretLeftOutlined, CaretRightOutlined } from "@ant-design/icons";
import { useThemeSwitcher } from "react-css-theme-switcher";
import { Reciever } from "./messages/Receiver";
import { Handler } from "./messages/Handler";
import { TestHandler } from "./messages/TestHandler";
import { VSCHandler } from "./messages/VSCHandler";

interface AppProps {
  isTest: boolean;
  initialTarget: string;
}

const App: FC<AppProps> = ({ isTest, initialTarget }) => {
  const themeContext = useThemeSwitcher();
  const [graphData, setGraphData] = React.useState<GraphProps>({
    nodeData: [],
    linkData: [],
  });
  React.useState<Handler>(() => {
    const handler = isTest
      ? new TestHandler(new Reciever(setGraphData))
      : new VSCHandler(new Reciever(setGraphData));
    handler.requestDependents(initialTarget);
    return handler;
  });
  const [isDarkMode, setIsDarkMode] = React.useState();

  const toggleTheme = (isChecked: any) => {
    setIsDarkMode(isChecked);
    themeContext.switcher({
      theme: isChecked ? themeContext.themes.dark : themeContext.themes.light,
    });
  };

  return (
    <div className="App">
      <Layout style={{ minHeight: "100vh" }}>
        <Header>
          <Row align="middle">
            <Col span={1}>
              <Button
                type="primary"
                shape="circle"
                icon={<CaretLeftOutlined />}
              />
            </Col>
            <Col span={1}>
              <Button
                type="primary"
                shape="circle"
                icon={<CaretRightOutlined />}
              />
            </Col>
            <Col span={8} offset={1}>
              <AutoComplete
                dropdownMatchSelectWidth={252}
                style={{ width: 400, display: "block" }}
                onSearch={(value: string) => {
                  console.log(value);
                }}
              >
                <Input.Search placeholder="class name" enterButton />
              </AutoComplete>
            </Col>
            <Col span={4} offset={8}>
              <Slider
                defaultValue={3}
                min={1}
                max={20}
                style={{ width: "100px" }}
                tooltipPlacement={"right"}
              />
            </Col>
            <Col span={1}>
              <Switch
                checked={isDarkMode}
                checkedChildren="Light"
                unCheckedChildren="Dark"
                onChange={toggleTheme}
              />
            </Col>
          </Row>
        </Header>
        <Content>
          <Graph nodeData={graphData.nodeData} linkData={graphData.linkData} />
        </Content>
      </Layout>
    </div>
  );
};

export default App;
