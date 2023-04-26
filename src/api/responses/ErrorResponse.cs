using System;

namespace DVAN.API
{
    /// <summary>
    /// Error response containing the request endpoint and a error message.
    /// </summary>
    class ErrorResponse
    {
        /// <summary>
        /// The request endpoint.
        /// </summary>
        /// <example>api/endpoint/action</example>
        public string request { get; set; }

        /// <summary>
        /// The error message.
        /// </summary>
        /// <example>invalid request</example>
        public string error { get; set; }

        public ErrorResponse(string request, string message)
        {
            this.request = request;
            this.error = message;
        }
    }
}