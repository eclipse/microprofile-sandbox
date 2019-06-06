Though this project is not itself licensed under the Apache 2.0 license, it does include modifications of code that was originally released under ASL 2.0.

According to the [ASL 2.0 license](https://www.apache.org/licenses/LICENSE-2.0),  `Section 4. Redistribution`, our requirements are to:

1. Package the ASL 2.0 license text file somewhere in our JAR
2. Add a comment somewhere in the source header describing the source of our modification
3. Preserve the Spotify copyright in the source
4. Reship any NOTICE file from the source project

So, in turn, 

1. This folder will include the Apache 2.0 License file for packaging.
2. This will be done (initially in the same commit as the one introducing this file).
3. (same)
4. At the time of this writing, on 2018-09-14, the [Spotify source repo](https://github.com/spotify/dockerfile-maven/tree/4088bb5e1f70c1feb74e21f46316d2f4f182f4aa) 
does not include a NOTICE file, so therefore we don't need to repackage one.  
    * Note that the link above includes the SHA of *master*, rather than just the branch, which we copied from, so the record of the fact that this repository doesn't include a NOTICE is more permanent.

