# GitDiffSplit

This android app displays a list of PullRequests from the github repository https://github.com/boto/boto3.git. 
On selecting any Pull Request item from the list, using it's assocaited diff_url, a diff.txt file is generated.
A new activity is created where the file is read and parsed into a List of Diffs using the DiffParser https://github.com/ptlis/diff-parser.git
