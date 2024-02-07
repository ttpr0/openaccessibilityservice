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

def test_matrix():
    global demand, supply, routing, response
    data = {
        "demand": demand,
        "supply": supply,
        "routing": routing,
        "range_max": 200,
    }
    resp = requests.post(f"{API}/v1/core/matrix", json=data).json()
    assert "matrix" in resp
    access = resp["matrix"]
    assert len(access) == len(demand["demand_locations"])
    assert len(access[0]) == len(supply["supply_locations"])
    assert access[0][0] - 122.065 <= 0.001
    assert access[1][2] - 56.569 <= 0.001
    assert access[2][1] - 78.102 <= 0.001
    assert access[3][2] - 67.082 <= 0.001
    assert access[4][0] - 20.000 <= 0.001

def test_nearest():
    global demand, supply, routing, response
    data = {
        "demand": demand,
        "supply": supply,
        "routing": routing,
        "range_max": 200,
    }
    resp = requests.post(f"{API}/v1/core/nearest", json=data).json()
    assert "nearest" in resp
    access = resp["nearest"]
    assert len(access) == len(demand["demand_locations"])
    assert access[0]["id"] == 2
    assert access[0]["range"] - 53.852 <= 0.001
    assert access[1]["id"] == 0
    assert access[1]["range"] - 14.142 <= 0.001
    assert access[2]["id"] == 2
    assert access[2]["range"] - 53.852 <= 0.001
    assert access[3]["id"] == 0
    assert access[3]["range"] - 22.361 <= 0.001
    assert access[4]["id"] == 0
    assert access[4]["range"] - 20.000 <= 0.001

def test_k_nearest():
    global demand, supply, routing, response
    data = {
        "demand": demand,
        "supply": supply,
        "routing": routing,
        "range_max": 200,
        "count": 2
    }
    resp = requests.post(f"{API}/v1/core/k_nearest", json=data).json()
    assert "k_nearest" in resp
    access = resp["k_nearest"]
    assert len(access) == len(demand["demand_locations"])
    assert access[0][1]["id"] == 1
    assert access[0][0]["range"] - 53.852 <= 0.001
    assert access[1][0]["id"] == 0
    assert access[1][1]["range"] - 50.990 <= 0.001
    assert access[2][1]["id"] == 0
    assert access[2][0]["range"] - 53.852 <= 0.001
    assert access[3][0]["id"] == 0
    assert access[3][1]["range"] - 50.000 <= 0.001
    assert access[4][1]["id"] == 1
    assert access[4][0]["range"] - 20.000 <= 0.001

def test_catchment():
    global demand, supply, routing, response
    data = {
        "demand": demand,
        "supply": supply,
        "routing": routing,
        "range_max": 60,
    }
    resp = requests.post(f"{API}/v1/core/catchment", json=data).json()
    assert "catchment" in resp
    access = resp["catchment"]
    assert len(access) == len(demand["demand_locations"])
    assert len(access[0]) == 1
    assert 2 in access[0]
    assert len(access[1]) == 3
    assert len(access[2]) == 1
    assert 2 in access[2]
    assert len(access[3]) == 2
    assert 0 in access[3]
    assert 1 in access[3]
    assert len(access[4]) == 1
    assert 0 in access[4]
