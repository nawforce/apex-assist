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
  current: number;
  history: string[];
  depth: number;
}

const App: FC<AppProps> = ({ isTest, identifer }) => {
  const themeContext = useThemeSwitcher();
  const [isDarkMode, setIsDarkMode] = React.useState(false);
  const [focus, setFocus] = React.useState<Focus>({
    current: 0,
    history: [identifer],
    depth: 3,
  });
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
    setFocus({ current: focus.current, history: focus.history, depth });
    handler.requestDependents(focus.history[focus.current], depth);
  };

  const onRefocus = (identifer: string) => {
    let stack: string[] = focus.history.slice(0, focus.current + 1);
    let length = stack.push(identifer);
    setFocus({ current: length - 1, history: stack, depth: focus.depth });
    handler.requestDependents(identifer, focus.depth);
  };

  const debouncedChangeDepth = debounce(changeDepth, 300);

  const canBackward = () => focus.current > 0;
  const canForward = () => focus.current < focus.history.length - 1;

  const goBackward = () => {
    setFocus({
      current: focus.current - 1,
      history: focus.history,
      depth: focus.depth,
    });
    handler.requestDependents(focus.history[focus.current - 1], focus.depth);
  };

  const goForward = () => {
    setFocus({
      current: focus.current + 1,
      history: focus.history,
      depth: focus.depth,
    });
    handler.requestDependents(focus.history[focus.current + 1], focus.depth);
  };

  console.log(focus.history[focus.current]);

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
                  disabled={!canBackward()}
                  onClick={goBackward}
                  icon={<CaretLeftOutlined />}
                />
                <Button
                  type="primary"
                  shape="circle"
                  disabled={!canForward()}
                  onClick={goForward}
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
            focusIdentifier={focus.history[focus.current]}
          />
        </Content>
      </Layout>
    </div>
  );
};

export default App;
