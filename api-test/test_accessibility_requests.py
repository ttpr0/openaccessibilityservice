import requests

API = "http://localhost:5001"

base_request = {
    "demand": {
        "demand_locations": [[1.2, 2.4], [4.2, 5.3], [9.1, 3.5]],
        "demand_weights": [2.4, 2.1, 6.7]
    },
    "supply": {
        "supply_locations": [[1.2, 2.4], [4.2, 5.3], [9.1, 3.5]],
        "supply_weights": [2.4, 2.1, 6.7]
    },
    "routing": {
        "routing_provider": "distance"
    },
    "distance_decay": {
        "decay_type": "hybrid",
        "max_range": 900,
        "ranges": [300, 600, 900],
        "range_factors": [1.0, 0.5, 0.2]
    },
    "response": {
        "scale": True,
        "scale_range": [0, 100],
        "no_data_value": -9999
    }
}

valid_demands = [
    {
        "demand_locations": [[1.2, 2.4], [4.2, 5.3], [9.1, 3.5]],
        "demand_weights": [2.3, 4.1, 7.5],
    },
    {
        "demand_locations": [[1.2, 2.4], [4.2, 5.3], [9.1, 3.5]],
    }
]
invalid_demands = [
    {
        "demand_locations": [[1.2, 2.4, 2.1], [4.2], [9.1, 3.5]],
    },
    {
        "demand_locations": [[1.2, 2.4], [4.2, 5.3], [9.1, 3.5]],
        "demand_weights": [2.3, 4.1],
    },
    {
        "demand_weights": [2.3, 4.1, 4.4],
    },
    {
        "locations": [],
    }
]

def test_demand_params():
    global base_request, valid_demands, invalid_demands
    data = base_request.copy()
    for p in valid_demands:
        data["demand"] = p
        resp = requests.post(f"{API}/v1/accessibility/enhanced_2sfca", json=data).json()
        assert "access" in resp, p

    for p in invalid_demands:
        data["demand"] = p
        resp = requests.post(f"{API}/v1/accessibility/enhanced_2sfca", json=data).json()
        assert "status" in resp, p
        assert resp["status"] == 400, p

valid_supplies = [
    {
        "supply_locations": [[1.2, 2.4], [4.2, 5.3], [9.1, 3.5]],
        "supply_weights": [2.3, 4.1, 7.5],
    },
    {
        "supply_locations": [[1.2, 2.4], [4.2, 5.3], [9.1, 3.5]],
    }
]
invalid_supplies = [
    {
        "supply_locations": [[1.2, 2.4, 2.1], [4.2], [9.1, 3.5]],
    },
    {
        "supply_locations": [[1.2, 2.4], [4.2, 5.3], [9.1, 3.5]],
        "supply_weights": [2.3, 4.1],
    },
    {
        "locations": [],
    }
]

def test_supply_params():
    global base_request, valid_supplies, invalid_supplies
    data = base_request.copy()
    for p in valid_supplies:
        data["supply"] = p
        resp = requests.post(f"{API}/v1/accessibility/enhanced_2sfca", json=data).json()
        assert "access" in resp, p

    for p in invalid_supplies:
        data["supply"] = p
        resp = requests.post(f"{API}/v1/accessibility/enhanced_2sfca", json=data).json()
        assert "status" in resp, p
        assert resp["status"] == 400, p

valid_dacays = [
    {
        "decay_type": "hybrid",
        "ranges": [300, 600, 900],
        "range_factors": [1.0, 0.5, 0.2]
    },
    {
        "decay_type": "binary",
        "max_range": 900,
    },
    {
        "decay_type": "linear",
        "max_range": 900,
    }
]
invalid_decays = [
    {
        "decay_type": "hybrid",
        "max_range": 900,
    },
    {
        "decay_type": "linear",
        "ranges": [300, 600, 900],
        "range_factors": [1.0, 0.5, 0.2]
    },
    {
        "decay_type": "hybrid",
        "ranges": [300, 600, 900],
    },
    {
        "decay_type": "hybrid",
        "ranges": [300, 600, 900],
        "range_factors": [1.0, 0.5]
    },
    {
        "decay_type": "equal",
        "max_range": 900,
    }
]

def test_decay_params():
    global base_request, valid_dacays, invalid_decays
    data = base_request.copy()
    for p in valid_dacays:
        data["distance_decay"] = p
        resp = requests.post(f"{API}/v1/accessibility/enhanced_2sfca", json=data).json()
        assert "access" in resp, p

    for p in invalid_decays:
        data["distance_decay"] = p
        resp = requests.post(f"{API}/v1/accessibility/enhanced_2sfca", json=data).json()
        assert "status" in resp, p
        assert resp["status"] == 400, p

valid_responses = [
    {
        "scale": True,
    },
    {
        "scale": True,
        "scale_range": [0, 100],
        "no_data_value": -9999
    },
]
invalid_responses = [
    {
        "scale": True,
        "scale_range": [100, 0],
    },
    {
        "scale": True,
        "scale_range": [200],
        "no_data_value": -9999
    },
]

def test_response_params():
    global base_request, valid_responses, invalid_responses
    data = base_request.copy()
    for p in valid_responses:
        data["response"] = p
        resp = requests.post(f"{API}/v1/accessibility/enhanced_2sfca", json=data).json()
        assert "access" in resp, p

    for p in invalid_responses:
        data["response"] = p
        resp = requests.post(f"{API}/v1/accessibility/enhanced_2sfca", json=data).json()
        assert "status" in resp, p
        assert resp["status"] == 400, p
