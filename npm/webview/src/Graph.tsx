import {Component, createRef, RefObject} from 'react';
import * as d3 from 'd3';

type GraphProps = {

}

export default class Graph extends Component {

    private myRef: RefObject<HTMLDivElement>

    constructor(props: GraphProps) {
        super(props);
        this.myRef = createRef<HTMLDivElement>();
    }

    componentDidMount() {
        let accessToRef = d3.select(this.myRef.current);
        accessToRef.style("background-color", "green");
    }

    render() {
        return <>
            <div ref={this.myRef}>Testing2</div>
        </>
    }
}
