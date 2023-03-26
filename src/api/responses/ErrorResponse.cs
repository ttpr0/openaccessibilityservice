using System;

namespace DVAN.API
{
    class ErrorResponse
    {
        public string request { get; set; }

        public string error { get; set; }

        public ErrorResponse(string request, string message)
        {
            this.request = request;
            this.error = message;
        }
    }
}