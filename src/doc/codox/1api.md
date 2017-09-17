# API

The public API is provided by <code>zana.api</code>, which uses 
[Potemkin](https://github.com/ztellman/potemkin) to import functions 
from implementation namespaces. Nothing prevents you from using functions from
implementation namespaces directly, but there is no promise that functions
won't move to another implementation namespace
