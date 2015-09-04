#!/usr/bin/php
<?php

/**
 * Tool to fetch the task description for a given task config
 *
 * Requirements:
 *  - PHP 5.4 or above
 *  - PHP-XML-RPC (enable by uncommenting extension=xmlrpc.so in php.ini)
 *
 * Licenced under MIT-Licence by Klemens Schölhorn
 */

$server = "http://kernkraft.imn.htwk-leipzig.de/cgi-bin/autotool-0.9.6.cgi";
$seed = "123456789";

if($argc != 2) {
    fwrite(STDERR, "Usage: php " . $argv[0] . " *exported autolat task xml*\n");
    die(2);
}


// Read task configuration from given XML

try {
    $task = @new SimpleXMLElement($argv[1], 0, true);
} catch(Exception $e) {
    fwrite(STDERR, "Could not open '" . $argv[1] . "'\n");
    die(1);
}

if($task->getName() !== "autotoolnode") {
    fwrite(STDERR, "no valid xml given\n");
    die(3);
}

$type = (string) $task->tasktype["type_name"];
$config = (string) $task->taskconfiguration->conf_text;
$signature = (string) $task->taskconfiguration->signature;


// Prepare XML-RPC request

$params = [
    "Signed" => [
        "contents" => [$type, $config],
        "signature" => $signature
    ]
];

$request = xmlrpc_encode_request("get_task_instance", [$params, $seed], [
    "escaping" => "cdata",
    "encoding" => "utf-8"
]);


// Execute as POST request

$context = stream_context_create([
    "http" => [
        "method" => "POST",
        "header" => "Content-Type: text/xml",
        "content" => $request
    ]
]);

if(false === ($response = @file_get_contents($server, false, $context))) {
    fwrite(STDERR, "could not reach server\n");
    die(5);
}

// Print results

$result = xmlrpc_decode($response);
if($result && xmlrpc_is_fault($result)) {
    fwrite(STDERR, $result["faultString"] . " (" . $result["faultCode"] . ")\n");
    die(10);
} else if(empty($result[1])) {
    fwrite(STDERR, "empty result\n");
    die(11);
} else {
    $dom = new DOMDocument();
    if($dom->loadXML($result[1])) {
        echo $dom->documentElement->textContent;
    } else {
        fwrite(STDERR, "invalid xml description\n");
        die(12);
    }
}
