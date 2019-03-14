# GitDiffSplit

This android app displays a list of PullRequests from the github repository https://github.com/boto/boto3.git. 
On selecting any Pull Request item from the list, using it's assocaited diff_url, a diff.txt file is generated.
A new activity is created where the file is read and parsed into a List of Diffs using the DiffParser https://github.com/ptlis/diff-parser.git

Using this diffs list, two separate lists(from and to) are created to show a side-by-side split view.

Known issues:
There are some limitations of the DiffParser library which causes missing lines of code in Hunks.

Libraries used:
1. https://github.com/square/retrofit.git
2. https://github.com/ReactiveX/RxJava.git
3. https://github.com/ReactiveX/RxAndroid.git
4. https://github.com/JakeWharton/butterknife.git
5. https://github.com/mikepenz/Materialize.git
