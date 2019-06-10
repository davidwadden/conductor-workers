#!/usr/bin/env bash

set -e

usage() {
  echo
  echo "Usage: CONDUCTOR_URL=http://example.com $0 [init,destroy,refresh]"
  echo
  echo "Note: Change CONDUCTOR_URL to the environment of your choosing"
  echo "Note: Set CONDUCTOR_USERNAME and CONDUCTOR_PASSWORD with basic auth credentials"
  exit 1
}

defs_dir="$(dirname $0)"
workflows_dir="${defs_dir}/workflows"
tasks_dir="${defs_dir}/tasks"

create_workflows() {
    echo "  Workflows"
    for file in ${workflows_dir}/*.json; do
        wf_name="$(basename ${file} .json)"
        echo "      CREATING $wf_name"
        http -b -a ${CONDUCTOR_USERNAME}:${CONDUCTOR_PASSWORD} POST ${CONDUCTOR_URL}/api/metadata/workflow < $file
    done
}

create_tasks() {
    echo "  Tasks"
    for file in ${tasks_dir}/*.json; do
        task_name="$(basename ${file} .json)"
        echo "      CREATING $task_name"
        http -b -a ${CONDUCTOR_USERNAME}:${CONDUCTOR_PASSWORD} POST ${CONDUCTOR_URL}/api/metadata/taskdefs < $file
    done
}

delete_workflows() {
    echo "  Workflows"
    for file in ${workflows_dir}/*.json; do
        wf_name="$(basename ${file} .json)"
        echo "      DELETING $wf_name"
        http -b -a ${CONDUCTOR_USERNAME}:${CONDUCTOR_PASSWORD} DELETE ${CONDUCTOR_URL}/api/metadata/workflow/${wf_name}/1
    done
}

delete_tasks() {
    echo "  Tasks"
    for file in ${tasks_dir}/*.json; do
        task_name="$(basename ${file} .json)"
        echo "      DELETING $task_name"
	    http -b -a ${CONDUCTOR_USERNAME}:${CONDUCTOR_PASSWORD} DELETE ${CONDUCTOR_URL}/api/metadata/taskdefs/${task_name}
    done
}

if [[ -z "$1" ]]; then
  usage
fi

readonly local_conductor_url=http://localhost:8080
if [[ -z "${CONDUCTOR_URL}" ]]; then
  echo "Defaulting CONDUCTOR_URL to ${local_conductor_url}"
  export CONDUCTOR_URL=${local_conductor_url}
fi

echo
echo "CONDUCTOR_URL is set to ${CONDUCTOR_URL}"
echo "CONDUCTOR_USERNAME is set to ${CONDUCTOR_USERNAME:-<none>}"
echo

subcommand=$1; shift
case "$subcommand" in
  init)
    echo "-----------------------------------------"
    echo "#              INITIALIZE               #"
    echo "-----------------------------------------"
    create_tasks
    create_workflows
    ;;

  destroy)
    echo "-----------------------------------------"
    echo "#              DESTROY                  #"
    echo "-----------------------------------------"
    delete_workflows
    delete_tasks
    ;;

  refresh)
    echo "-----------------------------------------"
    echo "#              DESTROY                  #"
    echo "-----------------------------------------"
    delete_workflows
    delete_tasks
    echo
    echo "-----------------------------------------"
    echo "#              INITIALIZE               #"
    echo "-----------------------------------------"
    create_tasks
    create_workflows
    ;;

  \? )
    usage
    ;;
  * )
    usage
    ;;
esac
