using System;
using DVAN.Population;
using DVAN.Routing;
using DVAN.API;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.StaticFiles;
using Microsoft.Extensions.DependencyInjection;

var builder = WebApplication.CreateBuilder(new WebApplicationOptions {
    Args = args
});

builder.Services.AddControllers();

builder.Services.AddEndpointsApiExplorer();

var MyAllowSpecificOrigins = "_myAllowSpecificOrigins";
builder.Services.AddCors(options => {
    options.AddPolicy(name: MyAllowSpecificOrigins, builder => {
        builder.AllowAnyHeader().AllowAnyMethod().AllowAnyOrigin();
    });
});

var app = builder.Build();

app.UseCors(MyAllowSpecificOrigins);

//add static file provider
var provider = new FileExtensionContentTypeProvider();
provider.Mappings[".geojson"] = "application/json";
app.UseStaticFiles(new StaticFileOptions {
    ContentTypeProvider = provider
});

app.MapGet("/close", () => { app.StopAsync().Wait(); });

app.MapControllers();

RoutingManager.addRoutingProvider(new ORSProvider("http://localhost:8082"));
PopulationManager.loadPopulation("./files/population.csv");

app.Run("http://localhost:5000");
