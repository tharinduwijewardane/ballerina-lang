import ballerina/test;
import ballerina/io;

@test:Config{
    dataProvider:"ValueProvider"
}
function testAddingValues (string fValue, string sValue, string result) returns error? {

    var value1 = check int.convert(fValue);
    var value2 = check int.convert(sValue);
    var result1 = check int.convert(result);
    io:println("Input params: ["+fValue+","+sValue+","+result+"]");
    test:assertEquals(value1 + value2, result1, msg = "The sum is not correct");
    return;
}

// Data provider function
function ValueProvider() returns (string[][]) {
    return [["1", "2", "3"], ["10", "20", "30"], ["5", "6", "11"]];
}

@test:Config{
    dataProvider:"jsonDataProvider"
}
function testJsonObjects (json fValue, json sValue, json result) returns error? {
    json a = {"a": "a"};
    json b = {"b": "b"};
    json c = {"c": "c"};
    test:assertEquals(fValue, a, msg = "json data provider failed");
    test:assertEquals(sValue, b, msg = "json data provider failed");
    test:assertEquals(result, c, msg = "json data provider failed");
    return;
}

function jsonDataProvider() returns (json[][]) {
    return [[{"a": "a"}, {"b": "b"}, {"c": "c"}]];
}
