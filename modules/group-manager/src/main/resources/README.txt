====
    Copyright 2014 Internet2

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
====

- All of the config files which have ".example" in them are examples of the real config files, with the same name without the ".example" in the name
- So copy each of the *.example* files to a name without .example in it, and customize to your environment.
- e.g. copy sources.example.xml to sources.xml, then customize
- the .example shows you which files need customization, and will not prompt you to commit the real file to CVS (since it is ignored and not stored in CVS)
- note some files without .example also might need customization (e.g. grouper.properties)