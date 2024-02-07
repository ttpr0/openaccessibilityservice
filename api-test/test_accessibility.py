import requests

API = "http://localhost:5001"

# test configuration
demand = {
    "demand_locations": [[110, 20], [50, 110], [110, 120], [30, 100], [40, 140]],
    "demand_weights": [10, 15, 12, 20, 5]
}
supply = {
    "supply_locations": [[40, 120], [60, 60], [90, 70]],
    "supply_weights": [20, 25, 15]
}

# parameters
routing = {
    "routing_provider": "distance",
    "range_type": "distance",
}
response = {
    "scale": False,
}

def test_opportunity():
    global demand, supply, routing, response
    data = {
        "demand": demand,
        "supply": supply,
        "routing": routing,
        "decay": {
            "decay_type": "hybrid",
            "ranges": [20, 50, 100],
            "range_factors": [1.0, 0.5, 0.2]
        },
        "response": response
    }
    resp = requests.post(f"{API}/v1/accessibility/opportunity", json=data).json()
    assert "access" in resp
    access = resp["access"]
    assert len(access) == len(demand["demand_locations"])
    assert access[0] - 8.000 <= 0.001
    assert access[1] - 28.000 <= 0.001
    assert access[2] - 12.000 <= 0.001
    assert access[3] - 25.500 <= 0.001
    assert access[4] - 28.000 <= 0.001

def test_reachability():
    global demand, supply, routing, response
    data = {
        "demand": demand,
        "supply": supply,
        "routing": routing,
        "decay": {
            "decay_type": "hybrid",
            "ranges": [20, 50, 100],
            "range_factors": [1.0, 0.5, 0.2]
        },
        "response": response
    }
    resp = requests.post(f"{API}/v1/accessibility/reachability", json=data).json()
    assert "access" in resp
    access = resp["access"]
    assert len(access) == len(demand["demand_locations"])
    assert access[0] - 3.000 <= 0.001
    assert access[1] - 20.000 <= 0.001
    assert access[2] - 3.000 <= 0.001
    assert access[3] - 10.000 <= 0.001
    assert access[4] - 20.000 <= 0.001

def test_2sfca():
    global demand, supply, routing, response
    data = {
        "demand": demand,
        "supply": supply,
        "routing": routing,
        "catchment": 60,
        "response": response
    }
    resp = requests.post(f"{API}/v1/accessibility/2sfca", json=data).json()
    assert "access" in resp
    access = resp["access"]
    assert len(access) == len(demand["demand_locations"])
    assert access[0] - 0.405 <= 0.001
    assert access[1] - 1.620 <= 0.001
    assert access[2] - 0.405 <= 0.001
    assert access[3] - 1.214 <= 0.001
    assert access[4] - 0.500 <= 0.001

def test_enhanced_2sfca():
    global demand, supply, routing, response
    data = {
        "demand": demand,
        "supply": supply,
        "routing": routing,
        "distance_decay": {
            "decay_type": "hybrid",
            "ranges": [20, 50, 100],
            "range_factors": [1.0, 0.5, 0.2]
        },
        "response": response
    }
    resp = requests.post(f"{API}/v1/accessibility/enhanced_2sfca", json=data).json()
    assert "access" in resp
    access = resp["access"]
    assert len(access) == len(demand["demand_locations"])
    assert access[0] - 0.514 <= 0.001
    assert access[1] - 1.131 <= 0.001
    assert access[2] - 0.637 <= 0.001
    assert access[3] - 1.230 <= 0.001
    assert access[4] - 1.131 <= 0.001
