# Build build image
FROM mcr.microsoft.com/dotnet/sdk:7.0 AS build-env
WORKDIR /oas

COPY src ./

RUN dotnet restore
RUN dotnet publish -c Release -o out

# Build runtime image
FROM mcr.microsoft.com/dotnet/aspnet:7.0
WORKDIR /oas

COPY --from=build-env /oas/out .
COPY ./files /oas/files

EXPOSE 5000

ENTRYPOINT ["dotnet", "accessibilityservice.dll"]