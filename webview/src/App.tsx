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
import { vscodeAPI } from "./messages/VSCodeAPI";
import { debounce } from "ts-debounce";

interface AppProps {
  vscodeAPI: vscodeAPI;
  isTest: boolean;
  identifier: string;
  allIdentifiers: string[];
}

interface Focus {
  current: number;
  history: [string, string[]][];
  depth: number;
}

const App: FC<AppProps> = ({ vscodeAPI, isTest, identifier, allIdentifiers }) => {
  const themeContext = useThemeSwitcher();
  const [isDarkMode, setIsDarkMode] = React.useState(true);
  const [isCountVisible, setIsCountVisible] = React.useState(false)
  const [focus, setFocus] = React.useState<Focus>({
    current: 0,
    history: [[identifier, []]],
    depth: 2,
  });
  const [identifiers] = React.useState<{ value: string }[]>(() => {
    return allIdentifiers.map((id) => {
      return { value: id };
    });
  });

  const [graphData, setGraphData] = React.useState<GraphData>({
    nodeData: [],
    linkData: [],
  });
  const [handler] = React.useState<Handler>(() => {
    const reciever = new Reciever(setGraphData, setIsDarkMode);
    const handler = isTest
      ? new TestHandler(reciever)
      : new VSCHandler(vscodeAPI, reciever, document);
    handler.requestDependents(identifier, 2, []);
    return handler;
  });

  const toggleTheme = (isChecked: any) => {
    setIsDarkMode(isChecked);
  };

  React.useEffect(() => {
    themeContext.switcher({
      theme: isDarkMode ? themeContext.themes.dark : themeContext.themes.light,
    });
  });

  const changeDepth = (depth: number) => {
    setFocus({
      current: focus.current,
      history: focus.history,
      depth,
    });
    const current = focus.history[focus.current];
    handler.requestDependents(current[0], depth, current[1]);
  };

  const onRefocus = (identifer: string) => {
    const stack: [string, string[]][] = focus.history.slice(
      0,
      focus.current + 1
    );
    const hide = stack[stack.length - 1][1];
    const length = stack.push([identifer, hide]);
    setFocus({
      current: length - 1,
      history: stack,
      depth: focus.depth,
    });
    setSearching(identifer);
    handler.requestDependents(identifer, focus.depth, hide);
  };

  const onOpen = (identifier: string) => {
    handler.openIdentifier(identifier);
  };

  const onHide = (identifier: string) => {
    if (identifier !== focus.history[focus.current][0]) {
      const stack: [string, string[]][] = focus.history.slice(
        0,
        focus.current + 1
      );
      const hide = [identifier].concat(stack[stack.length - 1][1]);
      const length = stack.push([stack[stack.length - 1][0], hide]);
      setFocus({
        current: length - 1,
        history: stack,
        depth: focus.depth,
      });

      handler.requestDependents(
        focus.history[focus.current][0],
        focus.depth,
        hide
      );
    }
  };

  const onToggleCount = () => {
    setIsCountVisible(!isCountVisible)
  }

  const debouncedChangeDepth = debounce(changeDepth, 300);

  const canBackward = () => focus.current > 0;
  const canForward = () => focus.current < focus.history.length - 1;

  const goBackward = () => {
    setFocus({
      current: focus.current - 1,
      history: focus.history,
      depth: focus.depth,
    });
    const current = focus.history[focus.current - 1];
    handler.requestDependents(current[0], focus.depth, current[1]);
    setSearching(current[0]);
  };

  const goForward = () => {
    setFocus({
      current: focus.current + 1,
      history: focus.history,
      depth: focus.depth,
    });
    const current = focus.history[focus.current + 1];
    handler.requestDependents(current[0], focus.depth, current[1]);
    setSearching(current[0]);
  };

  const [options, setOptions] = React.useState<{ value: string }[]>([]);
  const [searching, setSearching] = React.useState<string>(
    focus.history[focus.current][0]
  );

  const onSearch = (value: string) => {
    setSearching(value);
    if (value.length === 0) setOptions(identifiers);
    else setOptions(identifiers.filter((id) => id.value.startsWith(value)));
  };

  const onChange = (value: string) => {
    setSearching(value);
    if (identifiers.filter((id) => id.value === value).length > 0) {
      onRefocus(value);
    }
  };

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
                defaultValue={focus.history[focus.current][0]}
                value={searching}
                options={options}
                style={{ width: 400, display: "block" }}
                onSearch={onSearch}
                onChange={onChange}
              >
                <Input.Search placeholder="class name" enterButton />
              </AutoComplete>
            </Col>
            <Col span={4} offset={8}>
              <Slider
                defaultValue={2}
                min={1}
                max={10}
                style={{ width: "100px" }}
                tooltipPlacement={"right"}
                tipFormatter={(value) => `Depth: ${value}`}
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
            isCountVisible={isCountVisible}
            onRefocus={onRefocus}
            onOpen={onOpen}
            onHide={onHide}
            onToggleCount={onToggleCount}
            focusIdentifier={focus.history[focus.current][0]}
          />
        </Content>
      </Layout>
    </div>
  );
};

export default App;
