import {EditorView} from "@codemirror/view"
import {basicSetup} from "codemirror";
import {oneDark, oneDarkTheme} from "@codemirror/theme-one-dark";

import "./style.css";

const params = new URLSearchParams(window.location.search);
const ip = params.get("ip") || "localhost";
const port = params.get("port") || "8080";

const socket = new WebSocket(`ws://${ip}:${port}`);

let view = new EditorView({
    extensions: [basicSetup, oneDark, oneDarkTheme],
    parent: document.body
});

socket.onmessage = (event) => {
    view.dispatch({
        changes: {
            from: 0,
            to: view.state.doc.length,
            insert: event.data
        }
    });
};

window.onbeforeunload = () => {
    socket.send(view.state.doc.toString());
    socket.close();
    return null;
};