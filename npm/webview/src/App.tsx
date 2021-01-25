import "./App.css";
import data from "./data.json";
import Graph from "./components/Graph";
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
import React from "react";
import { CaretLeftOutlined, CaretRightOutlined } from "@ant-design/icons";
import { useThemeSwitcher } from "react-css-theme-switcher";

function App() {
  const [isDarkMode, setIsDarkMode] = React.useState();
  const { switcher, currentTheme, status, themes } = useThemeSwitcher();

  const toggleTheme = (isChecked: any) => {
    setIsDarkMode(isChecked);
    switcher({ theme: isChecked ? themes.dark : themes.light });
  };

  // Avoid theme change flicker
  //if (status === "loading") {
  //  return null;
  //}

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
            <Col span={1} offset={8} style={{ color: "white" }}>
              Depth
            </Col>
            <Col span={3}>
              <Slider
                defaultValue={3}
                min={1}
                max={20}
                style={{ width: "100px" }}
                tooltipVisible={true}
                tooltipPlacement={"right"}
              />
            </Col>
            <Col span = {1}>
              <Switch checked={isDarkMode} checkedChildren="Light" unCheckedChildren="Dark" onChange={toggleTheme} />
            </Col>
          </Row>
        </Header>
        <Content>
          <Graph nodeData={data.nodes} linkData={data.links} />
        </Content>
      </Layout>
    </div>
  );
}

export default App;
