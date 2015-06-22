#!/usr/bin/env bash

# Copied and modified from https://gist.github.com/sbellware/74a8090fc73c871c016b

projections=(
  "by_event_type"
)

echo
echo "Enabling Projections"
echo "= = ="
echo

for name in "${projections[@]}"; do
  echo
  echo "Enabling $name"
  echo "- - -"

  if [[ ! "$name" == "users" ]]; then
    name="%24$name"
    curl -i -X POST -d '' http://localhost:2113/projection/${name}/command/enable -u admin:changeit
  else
    echo "$name is enabled by default. Skipping."
  fi

  echo
  echo
done

echo "= = ="
echo "done"
echo