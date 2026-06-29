@echo off
setlocal enabledelayedexpansion

REM ============================================
REM CONFIGURACIÓN
REM ============================================

REM Nombre de la imagen Docker
set IMAGE_NAME=tp1-inventory-backend

REM Tag de la imagen
set IMAGE_TAG=latest

REM Registry opcional (déjalo vacío si solo quieres build local)
set REGISTRY=

REM Ruta del Dockerfile
set DOCKERFILE_PATH=Dockerfile

echo.
echo ============================================
echo  [1/2] Construyendo imagen Docker...
echo ============================================
echo.

docker build -f %DOCKERFILE_PATH% -t %IMAGE_NAME%:%IMAGE_TAG% .
if errorlevel 1 (
    echo.
    echo *** ERROR: El docker build falló.
    echo Revisa el Dockerfile o el código del proyecto.
    echo.
    exit /b 1
)

echo.
echo ============================================
echo  [2/2] (Opcional) Push a registry...
echo ============================================
echo.

if not "%REGISTRY%"=="" (
    set FULL_IMAGE=%REGISTRY%/%IMAGE_NAME%:%IMAGE_TAG%
    echo Etiquetando imagen como: %FULL_IMAGE%
    docker tag %IMAGE_NAME%:%IMAGE_TAG% %FULL_IMAGE%

    echo Subiendo imagen al registry...
    docker push %FULL_IMAGE%
) else (
    echo Registry vacío. Se omite el push remoto.
)

echo.
echo ============================================
echo     Build completado correctamente
echo ============================================
echo.
echo Imagen generada: %IMAGE_NAME%:%IMAGE_TAG%
echo.

endlocal
exit /b 0
