#!/usr/bin/env bash

# Since Carlisle launches herself after a git pull there should be some way to mitigate possible code failures.
# If broken code is uploaded to main and Carlisle's service tries to launch it then it could bring the whole service down.
# What I want to do is have Carlisle make a jar of her code after a successful run, then if a launch fails relaunch the old working jar.
# A very pseudocode version of this is below.

if lein run; # if Carlisle's service exits with Code 0
    then lein uberjar # note, args to uberjar determin what the main class is, I'd prefer having a way to name the jar (something like "latest.jar")
    else java -jar target/latest.jar # I think this is how it'd work? unsure. I'd also like to modify main to take an arg of last-run
        # if last-run is "failure" Carlisle should send a DM to owner noting this.
fi
