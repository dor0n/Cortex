#!/usr/bin/env bash

for JOB
do
	if [[ -d "${JOB}" ]]; then
		if [[ -r "${JOB}/input/input.json" ]]; then
			INPUT=$(cat ${JOB}/input/input.json)
		else
			INPUT="{}"
		fi
		DATA=$(jq .data <<< ${INPUT})
		DATATYPE=$(jq .dataType <<< ${INPUT})

		mkdir -p "${JOB}/output"
		cat > "${JOB}/output/output.json" <<- EOF
		{
			"success": true,
			"summary": {
				"taxonomies": [
					{ "namespace": "test", "predicate": "data", "value": "echo", "level": "info" }
				]
			},
			"full": ${INPUT},
			"operations": [
				{ "type": "AddTagToCase", "tag": "From Action Operation" },
				{ "type": "CreateTask", "title": "task created by action", "description": "yop !" }
			]
		}
EOF
	fi
done
