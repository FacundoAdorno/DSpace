

# Herramienta de Transformacion

Para usar y probar la herramienta hay que acceder a '/transformer/selectionPage' (el nombre me quedo viejo jejej)
Ahi encontraran un input donde tendran que escribir la consulta y un boton de submit
Hay 2 tipos de consultas disponibles:
*Seleccion
*Transformacion

Para ver los resultados de la seleccion se debe mirar el dri, para ver los resultados de la transformacion se puede acceder al item,
coleccion o comunidad modificado y checkear que se hayan realizado los cambios.

Seleccion:
Para usar esta funcionalidad no es necesario estar logueado. A traves de una consulta de seleccion se puede seleccionar
Items, colecciones y comunidades. Se pueden aplicar filtros ya sea por handle o por igualdad, mayor, menor o like de metadatos. Varios
filtros pueden ser aplicados simultaneamente.

El formato de la consulta es el siguiente:
seleccionar:item(condicion1 ,condicion2)

La primer palabra indica el tipo de actividad, en este caso seleccionar. Luego de los dos puntos ':' viene el tipo de DSO que se queire
seleccionar 'item', 'coleccion' o 'comunidad'. Luego dentro de los parentesis van las condiciones por las cuales se quire seleccionar,
en caso de ser mas de una se separan por coma ','. No hay restricciones en cuanto a los espacios entre las condiciones ni en su orden!

*Ej seleccion de un item por su handle:
seleccionar:item(handle= 11746/3184)
*Ej seleccion de items que pertenezcan a una collecion:
seleccionar:item(handle =11746/43)
*Ej seleccion de items cuyo dc.title sea identico a un valor:
seleccionar:item(dc.title=un titulo )
*Ej seleccion de items cuyo dc.title contenga un valor(like):
seleccionar:item(dc.title~un titulo)
*Ej seleccion de items cuyo dcterms.extent sea mayor a un valor:
seleccionar:item(dcterms.extend > 123)
*Ej seleccion de items cuyo dcterms.extent sea menor a un valor:
seleccionar:item(dcterms.extend < 123)
*Ej seleccion de items en base a multiples condiciones:
seleccionar:item(dc.title~ titulo, dc.abstract=valor especifico)

Para seleccionar colecciones o comunidades basta con cambiar la palabra clave 'item' por 'coleccion' o 'comunidad'

Transformacion:

Para usar esta funcionalidad es necesario estar logueado!! A traves de una consulta de seleccion se puede modificar, agregar y eliminar
metadatos de items, colecciones y comunidades. La primera parte de la consulta contendra una consulta de seleccion (con la misma
funcionalidad que fue explicado arriba) y la segunda parte contendra la consulta de transformacion, permitiendo el uso de expresiones 
regulares. Tambien permite elegir si se quiere trnasformar solo el primer matcheo(replace first) o todos los matcheos(replace all)

El formato de una transformacion es el siguiente:

transformarFirst:item(condicionDeSeleccion1, condicionDeSeleccion2 - condicionDeTransformacion1 , condicionDeTransformacion2)

La primera palabra indica el tipo de accion que realizara la consulta, hablando de transformaciones la herramienta permite 4 acciones:
*Transformar la primera ocurrenecia: 'transformarFirst'
*Transformar todas las ocurrencias: 'transformarAll'
*agregar un metadatato: 'agregar'
*eliminar un metadato: 'eliminar'
Luego de los dos puntos ':' se indica el tipo de DSO a transformar, en esta caso 'item'. Dentro de los parentesis encontramos 2 secciones,
las cuales estan separadas por un guion medio '-', la primera seccion son las condiciones de seleccion (iguales a lo expilcado en la
consulta de seleccion) la segunda seccion son las condiciones de transformacion las cuales tienen el siguiente formato:
metadato;expresion regular;nuevo valor. La condicion de trasnformacion esta conformada por 3 partes, separadas por punto y coma ';' 
la primera parte es el nombre del metadato, la seguna es una expresion regular (esta puede omitirse, en tal caso se reemplazara
todo el contenido del metadato por el nuevo valor), finalmente la tercera parte es el nuevo valor a poner.

*Ej transformacion del dc.title de un item seleccionado por su handle:
transformarFirst:item(handle=11746/3184 - dc.title;nuevo valor)
Esta consulta seleccionara el item cuyo handle sea 11746/3184 y luego reemplazara el titulo por 'nuevo valor'
*Ej transformacion del dc.title de un item seleccionado por su handle si cumple una condicion:
transformarFirst:item(handle=11746/3184 - dc.title;preuba;prueba)
Esta consulta seleccionara el item cuyo handle sea 11746/3184 y luego si encuentra la palabra 'preuba' la reemplazara por prueba
Cabe destacar que si la palabra 'preuba' aparece mas de una vez esta consulta reemplazara solo la primera ocurrencia!!
Para reemplazar todas las ocurrencias la consulta es:
transformarAll:item(handle=11746/3184 - dc.title;preuba;prueba)
*Ej agregar el metadato dc.title al item seleccionado por su handle:
agregar:item(handle=11746/3184 - dc.title;nuevo titulo)
*Ej eliminar el metadato dc.title al item seleccionado por su handle:
eliminar:item(handle=11746/3184 - dc.title)

Finalmente el modulo de transformacion permite poner el valor de otro metadato, permitiendo por ejemplo reemplazar el valor de un metadato
por otro:
*transformarFirst:item(handle=11746/3184 - dcterms.abstract;$dc.title;nuevo contenido)
Esta consulta selecciona un item por handle y luego busca en el metadato 'dcterms.abstract' la primera ocurrencia del contenido del metadato
'dc.title', si la encuentra la reemplaza por 'nuevo contenido'.

Al igual que en la seleccion se puede transformar varios metadatos en simultaneo
transformarFirst:item(handle=11746/3184 - dc.title;prueba , dc.abstract;vlor;valor)
Esta consulta seleccionara el item cuyo handle es 11746/3184 y reepmlazara el contenido del titulo por 'prueba'
y reemplzara la primera ocurrencia de 'vlor' que encuentre en el abstract por 'valor'

Cabe destacar que si en una misma consulta se realizan varias operaciones y al menos un falla se hace un roolback y se corta la ejecucion.



# DSpace

[![Build Status](https://travis-ci.org/DSpace/DSpace.png?branch=master)](https://travis-ci.org/DSpace/DSpace)

[DSpace Documentation](https://wiki.duraspace.org/display/DSDOC/) | 
[DSpace Releases](https://github.com/DSpace/DSpace/releases) |
[DSpace Wiki](https://wiki.duraspace.org/display/DSPACE/Home) | 
[Support](https://wiki.duraspace.org/display/DSPACE/Support)

DSpace open source software is a turnkey repository application used by more than 
1000+ organizations and institutions worldwide to provide durable access to digital resources.
For more information, visit http://www.dspace.org/

## Downloads

The latest release of DSpace can be downloaded from the [DSpace website](http://www.dspace.org/latest-release/) or from [GitHub](https://github.com/DSpace/DSpace/releases).

Past releases are all available via GitHub at https://github.com/DSpace/DSpace/releases

## Documentation / Installation

Documentation for each release may be viewed online or downloaded via our [Documentation Wiki](https://wiki.duraspace.org/display/DSDOC/). 

The latest DSpace Installation instructions are available at:
https://wiki.duraspace.org/display/DSDOC6x/Installing+DSpace

Please be aware that, as a Java web application, DSpace requires a database (PostgreSQL or Oracle) 
and a servlet container (usually Tomcat) in order to function.
More information about these and all other prerequisites can be found in the Installation instructions above.

## Contributing

DSpace is a community built and supported project. We do not have a centralized development or support team, 
but have a dedicated group of volunteers who help us improve the software, documentation, resources, etc.

We welcome contributions of any type. Here's a few basic guides that provide suggestions for contributing to DSpace:
* [How to Contribute to DSpace](https://wiki.duraspace.org/display/DSPACE/How+to+Contribute+to+DSpace): How to contribute in general (via code, documentation, bug reports, expertise, etc)
* [Code Contribution Guidelines](https://wiki.duraspace.org/display/DSPACE/Code+Contribution+Guidelines): How to give back code or contribute features, bug fixes, etc.
* [DSpace Community Advisory Team (DCAT)](https://wiki.duraspace.org/display/cmtygp/DSpace+Community+Advisory+Team): If you are not a developer, we also have an interest group specifically for repository managers. The DCAT group meets virtually, once a month, and sends open invitations to join their meetings via the [DCAT mailing list](https://groups.google.com/d/forum/DSpaceCommunityAdvisoryTeam).

We also encourage GitHub Pull Requests (PRs) at any time. Please see our [Development with Git](https://wiki.duraspace.org/display/DSPACE/Development+with+Git) guide for more info.

In addition, a listing of all known contributors to DSpace software can be
found online at: https://wiki.duraspace.org/display/DSPACE/DSpaceContributors

## Getting Help

DSpace provides public mailing lists where you can post questions or raise topics for discussion.
We welcome everyone to participate in these lists:

* [dspace-community@googlegroups.com](https://groups.google.com/d/forum/dspace-community) : General discussion about DSpace platform, announcements, sharing of best practices
* [dspace-tech@googlegroups.com](https://groups.google.com/d/forum/dspace-tech) : Technical support mailing list. See also our guide for [How to troubleshoot an error](https://wiki.duraspace.org/display/DSPACE/Troubleshoot+an+error).
* [dspace-devel@googlegroups.com](https://groups.google.com/d/forum/dspace-devel) : Developers / Development mailing list

Additional support options are listed at https://wiki.duraspace.org/display/DSPACE/Support

DSpace also has an active service provider network. If you'd rather hire a service provider to 
install, upgrade, customize or host DSpace, then we recommend getting in touch with one of our 
[Registered Service Providers](http://www.dspace.org/service-providers).

## Issue Tracker

The DSpace Issue Tracker can be found at: https://jira.duraspace.org/projects/DS/summary

## License

DSpace source code is freely available under a standard [BSD 3-Clause license](https://opensource.org/licenses/BSD-3-Clause).
The full license is available at http://www.dspace.org/license/
