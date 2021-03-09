import { GraphData } from "../components/Graph";
import { Handler } from "./Handler";
import { Reciever } from "./Receiver";

export class TestHandler implements Handler {
  private reciever: Reciever;

  private dependents: GraphData = {
		"nodeData": [{
			"name": "Cars"
		}, {
			"name": "fflib_QueryFactory"
		}, {
			"name": "Drivers"
		}, {
			"name": "fflib_SObjectDescribe"
		}, {
			"name": "Contestants"
		}, {
			"name": "CarsSelector"
		}, {
			"name": "ContestantsSelector"
		}, {
			"name": "TeamsSelector"
		}, {
			"name": "fflib_SObjectUnitOfWork"
		}, {
			"name": "fflib_StringBuilder"
		}, {
			"name": "fflib_SObjectSelector"
		}, {
			"name": "fflib_SecurityUtils"
		}, {
			"name": "RacesSelector"
		}, {
			"name": "fflib_SObjectDomain"
		}, {
			"name": "Races"
		}, {
			"name": "DriversSelector"
		}, {
			"name": "Teams"
		}, {
			"name": "RaceDataSelector"
		}, {
			"name": "Application"
		}],
		"linkData": [{
			"source": 1,
			"target": 0
		}, {
			"source": 2,
			"target": 0
		}, {
			"source": 3,
			"target": 2
		}, {
			"source": 4,
			"target": 2
		}, {
			"source": 5,
			"target": 2
		}, {
			"source": 6,
			"target": 2
		}, {
			"source": 7,
			"target": 2
		}, {
			"source": 8,
			"target": 1
		}, {
			"source": 9,
			"target": 1
		}, {
			"source": 2,
			"target": 1
		}, {
			"source": 10,
			"target": 1
		}, {
			"source": 11,
			"target": 1
		}, {
			"source": 12,
			"target": 1
		}, {
			"source": 13,
			"target": 1
		}, {
			"source": 14,
			"target": 1
		}, {
			"source": 15,
			"target": 1
		}, {
			"source": 16,
			"target": 1
		}, {
			"source": 17,
			"target": 1
		}, {
			"source": 0,
			"target": 1
		}, {
			"source": 5,
			"target": 1
		}, {
			"source": 18,
			"target": 1
		}]
	}

  constructor(reciever: Reciever) {
    this.reciever = reciever;
  }

  requestDependents(identifier: string, depth: number, hide?: string): void {
    setTimeout(() => {
      this.reciever.onTheme('vscode-dark')
      this.reciever.onDependents(this.dependents);
    }, 2000);
  }

  openIdentifier(identifier: string): void {
  }
}
