using System;
using DVAN.Population;
using DVAN.Routing;
using DVAN.API;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.StaticFiles;
using Microsoft.Extensions.DependencyInjection;
using System.Reflection;
using System.IO;

var builder = WebApplication.CreateBuilder(new WebApplicationOptions {
    Args = args
});

builder.Services.AddControllers();

builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(options => {
    // using System.Reflection;
    var xmlFilename = $"{Assembly.GetExecutingAssembly().GetName().Name}.xml";
    options.IncludeXmlComments(Path.Combine(AppContext.BaseDirectory, xmlFilename));
});

var MyAllowSpecificOrigins = "_myAllowSpecificOrigins";
builder.Services.AddCors(options => {
    options.AddPolicy(name: MyAllowSpecificOrigins, builder => {
        builder.AllowAnyHeader().AllowAnyMethod().AllowAnyOrigin();
    });
});

var app = builder.Build();

app.UseCors(MyAllowSpecificOrigins);

app.UseSwagger();
app.UseSwaggerUI();

//add static file provider
var provider = new FileExtensionContentTypeProvider();
provider.Mappings[".geojson"] = "application/json";
app.UseStaticFiles(new StaticFileOptions {
    ContentTypeProvider = provider
});

app.MapGet("/close", () => { app.StopAsync().Wait(); });

app.MapControllers();

RoutingManager.addRoutingProvider(new ORSProvider("http://172.26.62.41:8080/ors"));
PopulationManager.loadPopulation("./files/population.csv");
PopulationManager.periodicClearViewStore(TimeSpan.FromSeconds(60), TimeSpan.FromMinutes(5));

app.Run("http://localhost:5000");
