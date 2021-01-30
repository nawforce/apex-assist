import "./App.css";
import Graph, { GraphData } from "./components/Graph";
import {
  AutoComplete,
  Button,
  Col,
  Input,
  Layout,
  Row,
  Slider,
  Space,
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
import { debounce } from "ts-debounce";

interface AppProps {
  isTest: boolean;
  identifer: string;
}

interface Focus {
  identifer: string;
  depth: number;
}

const App: FC<AppProps> = ({ isTest, identifer }) => {
  const themeContext = useThemeSwitcher();
  const [isDarkMode, setIsDarkMode] = React.useState(false);
  const [focus, setFocus] = React.useState<Focus>({ identifer, depth: 3 });
  const [graphData, setGraphData] = React.useState<GraphData>({
    nodeData: [],
    linkData: [],
  });
  const [handler] = React.useState<Handler>(() => {
    const handler = isTest
      ? new TestHandler(new Reciever(setGraphData))
      : new VSCHandler(new Reciever(setGraphData));
    handler.requestDependents(identifer, 3);
    return handler;
  });

  const toggleTheme = (isChecked: any) => {
    setIsDarkMode(isChecked);
    themeContext.switcher({
      theme: isChecked ? themeContext.themes.dark : themeContext.themes.light,
    });
  };

  const changeDepth = (depth: number) => {
    setFocus({ identifer: focus.identifer, depth });
    handler.requestDependents(focus.identifer, depth);
  };

  const onRefocus = (identifer: string) => {
    setFocus({ identifer: identifer, depth: focus.depth });
    handler.requestDependents(identifer, focus.depth);
  };

  const debouncedChangeDepth = debounce(changeDepth, 300);

  return (
    <div className="App">
      <Layout style={{ minHeight: "100vh" }}>
        <Header>
          <Row align="middle">
            <Col span={1}>
              <Space>
                <Button
                  type="primary"
                  shape="circle"
                  icon={<CaretLeftOutlined />}
                />
                <Button
                  type="primary"
                  shape="circle"
                  icon={<CaretRightOutlined />}
                />
              </Space>
            </Col>
            <Col span={8} offset={2}>
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
                onAfterChange={debouncedChangeDepth}
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
          <Graph
            nodeData={graphData.nodeData}
            linkData={graphData.linkData}
            isDark={isDarkMode}
            onRefocus={onRefocus}
          />
        </Content>
      </Layout>
    </div>
  );
};

export default App;
