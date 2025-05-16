import subprocess
from flask import (
    Flask
)
from tools import token_required

config = {
    "DEBUG": True
}

app = Flask(__name__)
app.config.from_mapping(config)

@app.route("/api/protected")
@token_required
def protected():
    return "Protected content"

@app.route("/api/public")
def public():
    return "Public content"

if __name__ == '__main__':
    app.run(host="0.0.0.0")