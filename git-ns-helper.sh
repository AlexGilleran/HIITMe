#!/bin/bash
# Author: Andreas Borglin, Mike Gouline, Maciej Drozdzowski
# 2012
# Helper script for Neon Stingray Git/Gerrit usage.

VERSION=12
DEPLOYED='2014-01-19 23:43:08 UTC'

UPGRADE_CHECK_FILE=~/.git-ns-upgrade
VERSION_FILE=git-ns-version
SCRIPT_FILE=git-ns-helper.sh
OWNING_REPO=git-tools.git

# Configuration parameters
FILE_SERVER=http://review.neonstingray.com/files
GERRIT_URL=review.neonstingray.com
GERRIT_PORT=29418
PROFILE=~/.profile
SSH_CONFIG=~/.ssh/config
GERRIT_USERNAME=~/.gerrit-username
FEATURE_PREFIX=feature
RELEASE_PREFIX=release
MERGE_PREFIX=merge
DEVELOP=develop
MASTER=master
ORIGIN=origin
REFS=refs
ARCHIVE_REF=$REFS/archive
ARCHIVE_FEATURES=features
ARCHIVE_RELEASES=releases
ARCHIVE_EXPERIMENTAL=experimental

# Text style and colors
#txtund=$(tput sgr 0 1)          # Underline
#txtbld=$(tput bold)             # Bold
#bldred=${txtbld}$(tput setaf 1) #  red
#bldblu=${txtbld}$(tput setaf 4) #  blue
#txtrst=$(tput sgr0)             # Reset

# Print script usage to user
show_usage() {
    echo ""
    echo " * Neon Stingray Git Helper Script *"
    echo "   Version: $VERSION ($DEPLOYED)"
    echo ""
    echo "Available commands:"
    echo " -- Setup"
    echo "  setup           (Sets up Git/Gerrit for you)"
    echo "  upgrade         (Upgrade this script to latest version)"
    echo ""
    echo " -- Repository"
    echo "  clone           (Clones repo and sets up hooks)"
    echo ""
    echo " -- Topic Branches"
    echo "  start-topic     (Start a new topic branch for implementation or bug fixes)"
    echo "  delete-topic    (Delete topic branch once submitted via Gerrit)"
    echo ""
    echo " -- Commit / Upload Changes"
    echo "  commit          (Checks for current commit and amends it or creates a new one)"
    echo "  draft           (Pushes commit to a private draft area in Gerrit. Available flags: force, no-rebase.)"
    echo "  review          (Pushes commit for review to Gerrit.  Available flags: force, no-rebase.)"
    echo ""
    echo " -- Archives"
    echo "  laf             (List archived feature branches)"
    echo "  lar             (List archived release branches)"
    echo "  lae             (List archived experimental feature branches)"
    echo "  fetch-archive   (Fetch an archived branch)"
    echo ""
    echo " -- Utils / Other"
    echo "  lbm             (List which remote branches your local branches are tracking)"
    echo "  rm-deleted      (Remove all deleted files from repo)"
    echo "  track           (Select which remote branch to track for the current local branch)"
    echo "  revert          (Checkout a file from the currently tracked branch)"
}

# Parse the user provided command
parse_command() {
    if [ "$1" = "setup" ]
    then
        setup
    elif [ "$1" = "clone" ]
    then
        clone $2
    elif [ "$1" = "copy" ]
    then
        copy
    elif [ "$1" = "commit" ]
    then
        commit
    elif [ "$1" = "review" ]
    then
        review "review" $2 $3
    elif [ "$1" = "draft" ]
    then
        review "draft" $2 $3
    elif [ "$1" = "start-topic" ]
    then
        start_topic $2 $3
    elif [ "$1" = "delete-topic" ]
    then
        delete_topic $2
    elif [ "$1" = "lbm" ]
    then
        list_branch_mapping
    elif [ "$1" = "rm-deleted" ]
    then
        remove_deleted
    elif [ "$1" = "upgrade" ]
    then
        upgrade_requested
    elif [ "$1" = "track" ]
    then
        set_upstream
    elif [ "$1" = "laf" ]
    then
        list_archived_features
    elif [ "$1" = "lar" ]
    then
        list_archived_releases
    elif [ "$1" = "lae" ]
    then
        list_archived_experimentals
    elif [ "$1" = "fetch-archive" ]
    then
        fetch_archived_branch
    elif [ "$1" = "revert" ]
    then
        revert $2
    else
        echo "Unknown command."
        show_usage
    fi

}

# Clone a repository from Gerrit
clone() {
    if [ -z $1 ]
    then
        echo "Which repo would you like to clone?"
        read REPO_NAME

        die_if_empty "repo name" $REPO_NAME
    else
        REPO_NAME=$1
    fi

    git clone ns:/$REPO_NAME.git || die "Failed to clone repo $REPO_NAME"
    local USERNAME=$(cat "$GERRIT_USERNAME")
    # Install the commit msg hook in the repo
    curl $FILE_SERVER/commit-msg -OL
    mv commit-msg $REPO_NAME/.git/hooks
    chmod +x $REPO_NAME/.git/hooks/commit-msg

    # Track and switch to develop branch
    cd $REPO_NAME
    git branch --track $DEVELOP $ORIGIN/$DEVELOP || die "Repository $REPO_NAME created! (No develop branch)" # Silent exit since develop may not exist
    git checkout $DEVELOP || die "Failed to checkout $DEVELOP branch"

    echo "Repository $REPO_NAME created!"
}

# Switches between new commit and amend
commit() {
    # Is a rebase in progress?
    local TOP_LEVEL_DIR=$(git rev-parse --show-toplevel)
    local GIT_DIR="$TOP_LEVEL_DIR/.git"
    if [[ -d "$GIT_DIR/rebase-apply" || -d "$GIT_DIR/rebase-merge" ]]
    then
        die "It looks like you're in the middle of a rebase. Try executing git rebase --continue. Aborting."
    fi

    # Fetch the remote (origin) branch name and strip of origin/
    local REMOTEBRANCH=$(git rev-parse --abbrev-ref --symbolic-full-name @{u} | sed ' s/origin\/// ')

    # First, make sure there is only one commit that will get pushed
    # Get the SHA tag of the HEAD commit
    local SHA_PREV_COMMIT=$(git rev-parse HEAD)
    # Check if the commit exists on the remote branch
    local EXISTS_REMOTELY=$(git rev-list $ORIGIN/$REMOTEBRANCH | grep $SHA_PREV_COMMIT)
    if [ -z "$EXISTS_REMOTELY" ]
    then
        git commit --amend
    else
        git commit
    fi
}

# Submit commit for review, or as a draft
review() {

    local TYPE=$1
    local FLAG_1=$2
    local FLAG_2=$3

    if [ -n "$FLAG_1" ]
    then
        parse_review_parameter $FLAG_1 FORCE NO_REBASE
    fi

    if [ -n "$FLAG_2" ]
    then
        parse_review_parameter $FLAG_2 FORCE NO_REBASE
    fi

    if [ -n "$NO_REBASE" ]
    then
        echo "Rebase suppressed."
    else
        echo "Rebasing..."
        git pull --rebase
    fi

    # Fetch the local branch name
    local LOCALBRANCH=$(git branch 2> /dev/null | sed -e '/^[^*]/d' -e 's/* \(.*\)/\1/')
    # Fetch the remote (origin) branch name and strip of origin/
    local REMOTEBRANCH=$(git rev-parse --abbrev-ref --symbolic-full-name @{u} | sed ' s/origin\/// ')

    # First, make sure there is only one commit that will get pushed
    # Get the SHA tag of the commit preceeding HEAD
    local SHA_PREV_COMMIT=$(git rev-parse HEAD^1)
    # Check if the commit exists on the remote branch
    local EXISTS_REMOTELY=$(git rev-list $ORIGIN/$REMOTEBRANCH | grep $SHA_PREV_COMMIT)

    # If prev commit doesn't exist remotely, and no force flag is set, show error message
    if [ -z "$EXISTS_REMOTELY" ] && [ -z "$FORCE" ]
    then
        echo "You have more than one commit that would get pushed to Gerrit."
        echo "This may be caused by,"
        echo "  1. You haven't rebased (do a git pull --rebase)"
        echo "  2. Your topic is based off an unmerged change set (use the force flag as per below)"
        echo "  3. You created a new commit rather than amending (see below)"
        echo ""
        echo "For #3, you have 2 options"
        echo "  1. Do an interactive rebase and squash the commits: git rebase -i <base_commit>"
        echo "  2. Do a soft reset and amend the commit: git reset --soft <base_commit> , git commit -a --amend"
        echo ""
        echo "If you want to override this check, you can call this with a force flag 'git ns draft/review force'"
        die "Push to Gerrit aborted."
     # If force flag has been set, override the check
     elif [ -z "$EXISTS_REMOTELY" ] && [ -n "$FORCE" ]
     then
        echo "Forcing override of multiple commit check..."
     fi

    if [ "$TYPE" == "review" ]
    then
        MSG="Pushing to Gerrit for review..."
        REF=for
    else
        MSG="Pushing to Gerrit as a draft..."
        REF=drafts
    fi

    echo $MSG
    # Push for review
    git push $ORIGIN HEAD:refs/$REF/$REMOTEBRANCH/$LOCALBRANCH || die "Failed to push change to Gerrit"
    echo ""
    echo "Pushed local branch $LOCALBRANCH to Gerrit [remote branch $ORIGIN/$REMOTEBRANCH]"
}

# parses input parameters of review() and returns them via input arguments
parse_review_parameter() {
    local PARAM=$1
    local _FORCE_RESULT=$2
    local _NO_REBASE_RESULT=$3

    if [ -n "$PARAM" ] && [ "$PARAM" != "force" ] && [ "$PARAM" != "no-rebase" ]
    then
        die "Unknown flag: $PARAM"
    fi
    if [ -n "$PARAM" ]
    then
        if [ "$PARAM" == "force" ]
        then
            eval $_FORCE_RESULT="'true'"
        elif [ "$PARAM" == "no-rebase" ]
        then
            eval $_NO_REBASE_RESULT="'true'"
        fi
    fi
}

# Utility function for allowing user to select a remote branch
# Result is stored in input parameter variable via eval
read_remote_branch() {
    local _RESULTVAR=$1
    local BRANCHES
    i=0
    for key in $(git branch -r | tail -n +2)
    do
        (( ++i ))
        branch=${key/$ORIGIN\//}
        echo "$i - $branch"
        BRANCHES[$i]=$branch
    done

    read -p "Entry (1..$i): " REMOTE_BRANCH_INDEX

    die_if_empty "remote branch" $REMOTE_BRANCH_INDEX

    if [[ $REMOTE_BRANCH_INDEX != *[!0-9]* ]] && [ $REMOTE_BRANCH_INDEX -ge 1 ] && [ $REMOTE_BRANCH_INDEX -le $i ]
    then
        REMOTE_BRANCH_1=${BRANCHES[$REMOTE_BRANCH_INDEX]}
    else
        die "Entry out of range."
    fi

    eval $_RESULTVAR="'$REMOTE_BRANCH_1'"
}

start_topic() {
    local TOPIC_BRANCH

    if [ -z $2 ]
    then
        echo "Which remote branch should this be based off? (If the desired branch is not on the list, run 'git fetch' first)"

        read_remote_branch REMOTE_BRANCH

        echo ""
        echo "What should the topic branch be named? Should map to JIRA task/issue. Example: JIRA-123"
        read TOPIC_BRANCH

        die_if_empty "branch name" $TOPIC_BRANCH
    else
        REMOTE_BRANCH=$1
        die_if_empty "remote branch" $REMOTE_BRANCH

        TOPIC_BRANCH=$2
        die_if_empty "branch name" $TOPIC_BRANCH
    fi

    echo "$TOPIC_BRANCH will be created based off $REMOTE_BRANCH"
    continue_or_abort
    echo ""
    echo "Creating branch..."

    git branch --track $TOPIC_BRANCH $ORIGIN/$REMOTE_BRANCH || die "Failed to track remote branch $REMOTE_BRANCH. Sure it exists?"
    git checkout $TOPIC_BRANCH

    echo ""
    echo "Do not forget to start progress on the corresponding JIRA task/issue."
}

delete_topic() {
    if [ -z $1 ]
    then
        echo "Which topic branch do you want to delete? Example: JIRA-123"

        local BRANCHES
        i=0
        for key in $(git for-each-ref --format='%(refname:short)' refs/heads)
        do
            if [ "$key" != "master" ] && [ "$key" != "develop" ]
            then
                (( ++i ))
                branch=$key
                echo "$i - $branch"
                BRANCHES[$i]=$branch
            fi
        done

        read -p "Entry (1..$i): " TOPIC_BRANCH_INDEX

        die_if_empty "topic branch" $TOPIC_BRANCH_INDEX

        if [[ $TOPIC_BRANCH_INDEX != *[!0-9]* ]] && [ $TOPIC_BRANCH_INDEX -ge 1 ] && [ $TOPIC_BRANCH_INDEX -le $i ]
        then
            TOPIC_BRANCH=${BRANCHES[$TOPIC_BRANCH_INDEX]}
        else
            die "Entry out of range."
        fi
    else
        TOPIC_BRANCH=$1
    fi
    echo "Deleting topic branch..."
    git fetch
    git checkout $DEVELOP
    git branch -d $TOPIC_BRANCH || die "Failed to delete topic branch $TOPIC_BRANCH. Have the changes been merged?"
    echo "Branch $TOPIC_BRANCH deleted"
}

list_branch_mapping() {
    git for-each-ref --format='%(refname:short) <- %(upstream:short)' refs/heads
}

remove_deleted() {
    git ls-files -z --deleted | xargs -0 git rm
}

# checks out a given file from the currently tracked remote branch
revert() {
    local FILE=$1
    die_if_empty "file to revert" $FILE

    local REMOTE_BRANCH=$(git rev-parse --abbrev-ref --symbolic-full-name @{u})
    die_if_empty "tracked branch" $REMOTE_BRANCH

    git checkout $REMOTE_BRANCH -- $FILE

    echo "Changes to $FILE reverted."
}

set_upstream() {
    echo "Which remote branch do you want the current local branch to track? (Run git fetch if you can't find the branch you're looking for)"
    read_remote_branch BRANCH

    local LOCALBRANCH=$(git branch 2> /dev/null | sed -e '/^[^*]/d' -e 's/* \(.*\)/\1/')
    git branch -u "$ORIGIN/$BRANCH" &> /dev/null
    # If failed, likely due to Git version <1.8, then try old way of doing this
    if [ $? > 0 ]
    then
        git branch --set-upstream $LOCALBRANCH "$ORIGIN/$BRANCH"
    else
        echo "$LOCALBRANCH is now tracking $ORIGIN/$BRANCH"
    fi
}

# fetches & prints a list of archived feature branches
list_archived_features() {
    echo "Fetching archived features..."
    git ls-remote | grep $ARCHIVE_REF/$ARCHIVE_FEATURES
}

# fetches & prints a list of archived release branches
list_archived_releases() {
    echo "Fetching archived releases..."
    git ls-remote | grep $ARCHIVE_REF/$ARCHIVE_RELEASES
}

# fetches & prints a list of archived experimental feature branches
list_archived_experimentals() {
    echo "Fetching archived experimental features..."
    git ls-remote | grep $ARCHIVE_REF/$ARCHIVE_EXPERIMENTAL
}

# fetches an archived branch and potentially sets up a local topic branch off it
fetch_archived_branch() {
    local FILTER
    local FILTER_STRING

    echo "What kind of archived branch do you want to fetch?"
    echo "  1. Feature branch"
    echo "  2. Release branch"
    echo "  3. Experimental feature branch"

    read -p "Entry (1..3) " TYPE
    die_if_empty "archived branch type" $TYPE

    if [ "$TYPE" = "1" ]
    then
        FILTER=$ARCHIVE_REF/$ARCHIVE_FEATURES
        FILTER_STRING="feature branch"
    elif [ "$TYPE" = "2" ]
    then
        FILTER=$ARCHIVE_REF/$ARCHIVE_RELEASES
        FILTER_STRING="release branch"
    elif [ "$TYPE" = "3" ]
    then
        FILTER=$ARCHIVE_REF/$ARCHIVE_EXPERIMENTAL
        FILTER_STRING="experimental feature branch"
    else
        echo "Unknown option"
    fi

    echo ""
    echo "Which archived $FILTER_STRING do you want to fetch?"
    read_remote_ref $FILTER RESULT

    echo ""
    echo "Fetching $FILTER/$RESULT ..."
    git fetch origin $FILTER/$RESULT

    echo ""
    echo "Do you want to create a topic branch off the newly fetched ref? y/n"
    read CONTINUE

    if [ "$CONTINUE" == "y" ]
    then
        echo ""
        echo "What topic branch name do you want?"
        read TOPIC_NAME
        git checkout FETCH_HEAD -b $TOPIC_NAME
    else
        echo ""
        echo "--> Execute 'git checkout FETCH_HEAD' to use the newly fetched ref."
    fi
}

# Utility function for allowing user to select a ref from a list of filtered refs
# Result is stored in input parameter variable via eval
read_remote_ref() {
    local FILTER=$1
    local _RESULTVAR=$2
    local REFS
    local REMOTE_REFS

    echo "Fetching refs ..."

    i=0
    for key in $(git ls-remote $ORIGIN | grep $FILTER | awk '{print $2}')
    do
        (( ++i ))
        ref=${key/$FILTER\//}
        echo "$i - $ref"
        REFS[$i]=$ref
    done

    read -p "Entry (1..$i): " REMOTE_REF_INDEX

    die_if_empty "remote ref" $REMOTE_REF_INDEX

    if [[ $REMOTE_REF_INDEX != *[!0-9]* ]] && [ $REMOTE_REF_INDEX -ge 1 ] && [ $REMOTE_REF_INDEX -le $i ]
    then
        REMOTE_REF_SELECTED=${REFS[$REMOTE_REF_INDEX]}
    else
        die "Entry out of range."
    fi

    eval $_RESULTVAR="'$REMOTE_REF_SELECTED'"
}

# Setup the Git/Gerrit environment
setup() {
    clear

    if [ -f "$GERRIT_USERNAME" ]
    then
        SETUP_ALREADY_RUN=true
    fi

    # Users full name
    echo "What is your full name? Example: John Doe"
    read NAME
    die_if_empty "name" $NAME
    git config --global user.name "$NAME"

    # Users email
    echo ""
    echo "What is your Neon Stingray e-mail? Example: john.doe@neonstingray.com"
    read EMAIL
    die_if_empty "email" $EMAIL
    git config --global user.email $EMAIL

    # Username
    echo ""
    echo "What is your Gerrit username that you specified when registering?"
    read USERNAME
    die_if_empty "username" $USERNAME
    echo $USERNAME > "$GERRIT_USERNAME"

    # Preferred editor
    echo ""
    echo "Which editor would you like to use for Git commit messages? Examples: vi, vim, emacs, nano"
    read EDITOR
    die_if_empty "editor" $EDITOR
    git config --global core.editor $EDITOR

    # Set the color scheme for Git output
    git config --global color.ui auto

    # Set default push to tracking branches
    git config --global push.default tracking

    # Setup git completion
    echo ""
    echo "Downloading git completion script..."
    curl https://github.com/git/git/raw/master/contrib/completion/git-completion.bash -OL
    mv git-completion.bash ~/.git-completion.bash
    echo "source ~/.git-completion.bash" >> "$PROFILE"

    # Setup Git branch info in terminal
    echo ""
    echo "Downloading git console branch highlighting..."
    curl $FILE_SERVER/console_git_branch.bash -OL
    mv console_git_branch.bash ~/.console_git_branch.bash
    echo "source ~/.console_git_branch.bash" >> "$PROFILE"

    # Setup ssh-agent if running on msysgit (enabled by default for Mac/Linux)
    SYSTEM=$(uname)
    if [[ "$SYSTEM" == *MINGW* ]]
    then
        curl $FILE_SERVER/ms-ssh.bash -OL
        mv ms-ssh.bash ~/.ms-ssh.bash
        echo "source ~/.ms-ssh.bash" >> "$PROFILE"
        echo "Setting up ssh-agent...you need to restart Git Bash after this!"
    fi

    # Load the new profile
    source "$PROFILE"

    # Setup SSH config for Gerrit
    echo ""
    echo "Setting up SSH configs..."
    echo "Host ns" >> "$SSH_CONFIG"
    echo "  Hostname $GERRIT_URL" >> "$SSH_CONFIG"
    echo "  Port $GERRIT_PORT" >> "$SSH_CONFIG"
    echo "  User $USERNAME" >> "$SSH_CONFIG"

    # Copy the script to Git path
    echo ""
    copy

    echo ""
    echo "Setup done!"
    echo "You can now use 'git ns <command>'!"

}

# Copy script to Git path
copy() {
    echo "Installing script on git path..."

    local GIT_PATH=$(which git)
    local SUDO_AVAILABLE=$(which sudo 2> /dev/null)
 
    if [ -z $SUDO_AVAILABLE ]
    then 
        cp $SCRIPT_FILE "$GIT_PATH-ns" || die "Failed to copy script!"
    else
        echo "This operation might require admin access and might ask for your password."
        sudo cp $SCRIPT_FILE "$GIT_PATH-ns" || die "Failed to copy script!"
    fi

    echo "Script installed successfully at $GIT_PATH-ns."
    echo "You can now use: git ns <command> to call the script from anywhere."
}

die() {
    echo "$1"
    exit 1
}

die_if_empty() {
    if [ -z "$2" ]
    then 
       die "No $1 supplied. Aborting."
    fi
}

continue_or_abort() {
    echo "Do you want to continue (y/n)?"
    read CONTINUE

    if [ "$CONTINUE" != "y" ]
    then 
       die "Aborted"
    fi
}

# User requested upgrade check
upgrade_requested() {
    echo "Checking for updates..."
    check_server_version "1"
}

# Called when there is a new version
upgrade() {
    echo "New version of script available. Upgrading..."
    curl -OL --silent "$FILE_SERVER/$SCRIPT_FILE" || die "Failed to fetch script"

    local GIT_NS_PATH=$(which git-ns)

    # If git-ns is not installed yet, it presumably means that user is trying to install an old version for the first time
    if [ -z "$GIT_NS_PATH" ]
    then
        return
    fi

    local SUDO_AVAILABLE=$(which sudo 2> /dev/null)
 
    if [ -z $SUDO_AVAILABLE ]
    then
        chmod +x "$SCRIPT_FILE"
        mv "$SCRIPT_FILE" "$GIT_NS_PATH" || die "Failed to copy script!"
    else
        echo "Requires sudo and might require your password"
        sudo chmod +x "$SCRIPT_FILE"
        sudo mv "$SCRIPT_FILE" "$GIT_NS_PATH" || die "Failed to copy script!"
    fi

    echo "Successfully upgraded script from v$VERSION to v$SERVER_VERSION"
}

# Check if the server reports that there is a new version available
check_server_version() {
    local REPO=$(git config --get remote.origin.url | sed ' s!.*/!! ')
    # We never want to run update in the git-tools repo!
    if [ "$REPO" != "$OWNING_REPO" ]
    then
        # Fetch version silently
        local SERVER_VERSION=$(curl -s "$FILE_SERVER/$VERSION_FILE")
        if [ $SERVER_VERSION -gt $VERSION ] && [ $VERSION -gt 0 ]
        then
            upgrade $SERVER_VERSION
        else
            # If parameter set, provide user feedback
            if [ "$1" == "1" ]
            then
                echo "No new versions found."
            fi
        fi
    fi
}

# Check if it has been >24 hrs since last check
check_for_update() {

    local CUR_TIME=$(date +%s)
    if [ -f "$UPGRADE_CHECK_FILE" ]
    then
        local LAST_CHECK=$(cat "$UPGRADE_CHECK_FILE")
        # Only check for updates every 24 hours
        if [ $CUR_TIME -gt $(($LAST_CHECK + 86400)) ]
        then
            echo $CUR_TIME > "$UPGRADE_CHECK_FILE"
            check_server_version
        fi
    else
        echo $CUR_TIME > "$UPGRADE_CHECK_FILE"
    fi

}

# Check if there are any updates
check_for_update

# Check number of commands
if [ $# -ge 1 ]
then
    parse_command $@
else
    show_usage
fi
