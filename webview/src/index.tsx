import React from "react";
import ReactDOM from "react-dom";
import "./index.css";
import App from "./App";
import { ThemeSwitcherProvider } from "react-css-theme-switcher";

let themes = {
  dark: `/dark-theme.css`,
  light: `/light-theme.css`,
};

// Override theme location if we can see a prefetch (in VSCode template)
let lightPrefetch = document
  .getElementById("theme-prefetch-light")
  ?.getAttribute("href") as string;
let darkPrefetch = document
  .getElementById("theme-prefetch-dark")
  ?.getAttribute("href") as string;

if (lightPrefetch && darkPrefetch) {
  themes.light = lightPrefetch;
  themes.dark = darkPrefetch;
}

window.addEventListener("message", (event) => {
  ReactDOM.render(
    <ThemeSwitcherProvider
      themeMap={themes}
      defaultTheme="dark"
      insertionPoint="inject-styles-here"
    >
      <App
        isTest={event.data.isTest}
        identifier={event.data.identifier}
        allIdentifiers={event.data.allIdentifiers}
      />
    </ThemeSwitcherProvider>,
    document.getElementById("root")
  );
});
