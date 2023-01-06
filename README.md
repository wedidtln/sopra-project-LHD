<h1 align="center">
  <img src="./.github/img/Header.png" alt="EDT">
  <a href="./.github/file/ConsignesAttendus_du_projetS1-1.pdf">Subject</a>
  |
    <a href="./doc/report.pdf">Report</a>
  |
  <a href="./.github/file/Manuel.pdf">Manual</a>
</h1>

A small project from University, aiming to recreate a java planning app with an enphasis on modern technique
of software engineering.

- Continuous Integration with Github and SonarCloud
- <a href="https://docs.github.com/en/get-started/quickstart/github-flow">Github flow</a> and kanban
- An esthetic UI and exhaustive documentation

Features
--------

- Authentication of a User

  <img width="50%" height="50%" src="./.github/img/Login.png" alt="EDT">

- See planning of a User


  <img width="50%" height="50%" src="./.github/img/simpleView.png" alt="EDT">
  
  
- Manage planning


  <img width="50%" height="50%" src="./.github/img/manage.png" alt="EDT">

Install
--------------
*Requirement:* PostgreSQL,Java SDK 11+, Maven
  - Create a database `db_name` and initialize it with our script with `psql -d db_name < db_lhd.sql`
  - For now their is no Jar so compile and lauch the app using maven or your IDE

Project layout
--------------
```
.
├── .Github           CI script
├── doc               Javadoc/Manual/Report
├── edtLHD            Java src
├── LICENSE
└── ressources        Database init file
```
