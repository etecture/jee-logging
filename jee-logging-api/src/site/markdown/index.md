# Introduction

This module implements a generic logging facility for any Enterprise Java Application.

It uses CDI to implementing two ways for a developer to log whats going on in their application:

* directly inject a Log-Facade into a bean
* autologging of all method starts and stops via an interceptor

See [usage](usage.html) for details.
