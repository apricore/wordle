function Request(func) {
  return {
    resolves: [],
    then(resolve) {
      this.resolves.push(resolve);
    },
    resolve(result) {
      queueMicrotask(() => this.resolves.shift()?.(result));
    },
    async send() {
      if (this.connectPromise) {
        await this.connectPromise.promise;
        delete this.connectPromise;
        delete this.connect;
        this.send = async function () {
          func?.(...arguments);
          return await this;
        }
      }
      func?.(...arguments);
      return await this;
    },
    connectPromise: Promise.withResolvers(),
    connect() {
      this.connectPromise.resolve();
    }
  }
}
function create(tagName, attributes, callback) {
  let elem = document.createElement(tagName);
  for (let attribute in attributes)
    elem.setAttribute(attribute, attributes[attribute]);
  callback?.(elem);
  return elem;
}
class StompClient {
  constructor(url) {
    this.url = url;
    this.ws = new WebSocket(this.url);
    this.client = Stomp.over(this.ws);
    this.client.debug = null;
    this.client.connect({
      login: "", passcode: ""
    }, frame => {
      var subscription = this.client.subscribe("/user/queue/unique", message => {
        subscription.unsubscribe();
        this.client.subscribe("/queue/response-" + message.body, this.onmessage);
        request.connect();
      });
      this.sendTo("/app/sessionId", {});
    });
    this.ws.onclose = event => {
      location.reload();
    };
  }
  sendTo(destination, message) {
    this.client.send(destination, {}, JSON.stringify(message));
  }
  send(message) {
    this.sendTo("/app/send", message);
  }
  onmessage(message) {
    var message = JSON.parse(message.body);
    var eventHandler = eventHandlers[message.event];
    if (eventHandler) eventHandler(message);
    else console.log(message);
  }
}
const stompClient = new StompClient(location.origin.replace("http", "ws") + "/ws");
const request = Request(message => {
  stompClient.send(message);
});
const eventHandlers = {
  "create-room": message => {
    request.resolve(message);
  },
  "enter-room": message => {
    if (room.hidden) {
      request.resolve(message);
    } else {
      room.refresh(message.users);
    }
  },
  "input-word": message => {
    var user = message.user;
    if (!message.code || user.id == sessionStorage.wordleUserId) {
      request.resolve(message);
    } else {
      groupBody.refreshMember(user);
    }
  },
  "leave-room": message => {
    if (message.userId == sessionStorage.wordleUserId) {
      request.resolve(message);
    } else {
      groupBody.querySelector("#m" + message.userId).remove();
    }
  },
  "give-up": message => {
    if (message.answer) {
      request.resolve(message);
    } else if (message.user.id != sessionStorage.wordleUserId) {
      groupBody.querySelector("#m" + message.user.id).classList.add("give-up");
    }
  },
  "reset-word": message => {
    if (message.code === 1 && message.user.id != sessionStorage.wordleUserId) {
      groupBody.clear();
      container.clear();
      container.setWord(message.answer);
      if (message.user.state[0] === "_") groupBody.querySelector("#m" + message.user.id).classList.add("setter");
    } else {
      request.resolve(message);
    }
  }
};
create("div", {id: "pager"}, pager => {
  var hall = create("div", {id: "hall"});
  create("div", {id: "userinfo"}, userinfo => {
    var username = create("div", {id: "username", spellcheck: "false"});
    var pen = create("span", {id: "pen"});
    var nickname = localStorage.wordleUserName;
    if (!nickname || nickname === "Anonymous") {
      do {
        nickname = prompt("Plase entry you nickname:");
        if (nickname === null) nickname = "Anonymous";
        else nickname = nickname.trim().slice(0, 18);;
        if (!nickname) alert("Nickname cannot be empty!")
      } while (!nickname);
    }
    localStorage.wordleUserName = nickname;
    username.innerText = nickname;
    pen.onclick = event => {
      username.setAttribute("contenteditable", "plaintext-only");
      username.focus();
      var selection = document.getSelection();
      selection.empty();
      selection.selectAllChildren(username);
      username.onblur = event => {
        username.removeAttribute("contenteditable");
        username.onblur = null;
        if (!username.innerText) {
          username.innerText = "Anonymous";
        }
        var name = username.innerText.slice(0, 18);
        username.innerText = name;
        localStorage.wordleUserName = name;
      };
    };
    username.onkeypress = event => {
      if (event.key === "Enter") {
        username.blur();
      }
    };
    userinfo.append(username, pen);
    hall.append(userinfo);
  });
  create("div", {id: "btns"}, btns => {
    var createBtn = create("div", {id: "createBtn", class: "createBtn"});
    var enterBtn = create("div", {id: "enterBtn", class: "enterBtn"});
    createBtn.innerText = "Create Room";
    enterBtn.innerText = "Enter Room";
    createBtn.onclick = event => {
      overlay.show();
      inputForm.setCreateRoom();
    };
    enterBtn.onclick = event => {
      overlay.show();
      inputForm.setEnterRoom();
    };
    btns.append(createBtn, create("br"), enterBtn);
    hall.append(btns);
  });
  create("div", {id: "overlay"}, overlay => {
    var inputForm = create("form", {id: "inputForm", action: "javascript:void(0);"}, inputForm => {
      var legend = create("legend");
      var h3 = create("h3");
      var closeBtn = create("div", {class: "closeBtn"}, closeBtn => {
        closeBtn.onclick = () => {
          overlay.dismiss();
        };
      });
      var passwordEntry = create("div", {class: "entry"}, entry => {
        var label = create("label", {for: "password"});
        var input = create("input", {id: "password", name: "password"});
        var errorMessageDiv = create("ul", {class: "errorMessage"});
        label.innerText = "Password:";
        entry.append(label, errorMessageDiv, input);
        entry.setValue = value => {
          input.value = value;
        };
        entry.getValue = () => input.value.trim();
        entry.setErrorMessage = function () {
          var fragment = document.createDocumentFragment();
          for (let errorMessage of arguments) {
            let li = create("li");
            li.innerText = errorMessage;
            fragment.append(li);
          }
          errorMessageDiv.innerHTML = "";
          errorMessageDiv.append(fragment);
        };
      });
      var roomIdEntry = create("div", {class: "entry"}, entry => {
        var label = create("label", {for: "roomId"});
        var input = create("input", {id: "roomId", name: "roomId"});
        var errorMessageDiv = create("ul", {class: "errorMessage"});
        label.innerText = "Room id:";
        entry.append(label, errorMessageDiv, input);
        entry.setValue = value => {
          input.value = value;
        };
        entry.getValue = () => input.value.trim();
        entry.setErrorMessage = function () {
          var fragment = document.createDocumentFragment();
          for (let errorMessage of arguments) {
            let li = create("li");
            li.innerText = errorMessage;
            fragment.append(li);
          }
          errorMessageDiv.innerHTML = "";
          errorMessageDiv.append(fragment);
        };
      });
      var submitBtn = create("input", {type: "submit"});
      legend.append(h3);
      inputForm.append(legend, closeBtn, roomIdEntry, passwordEntry, submitBtn);
      inputForm.setCreateRoom = () => {
        h3.innerText = "Create Room";
        roomIdEntry.hidden = true;
        passwordEntry.setErrorMessage();
        submitBtn.value = "Create";
        submitBtn.className = "createBtn";
        password.focus();
      };
      inputForm.setEnterRoom = () => {
        h3.innerText = "Enter Room";
        roomIdEntry.hidden = false;
        roomIdEntry.setErrorMessage();
        passwordEntry.setErrorMessage();
        submitBtn.value = "Enter";
        submitBtn.className = "enterBtn";
        roomId.focus();
      };
      inputForm.onclick = event => {
        event.stopPropagation();
      };
      inputForm.onsubmit = event => {
        roomIdEntry.setErrorMessage();
        passwordEntry.setErrorMessage();
        submitBtn.disabled = true;
        if (inputForm[2].matches(".createBtn")) {
          let passKey = passwordEntry.getValue();
          if (!passKey) {
            passwordEntry.setErrorMessage("Cannot be empty!");
            passwordEntry.setValue("");
            submitBtn.disabled = false;
          } else {
            request.send({
              action: "create-room",
              password: passKey
            }).then(response => {
              submitBtn.disabled = false;
              if (response.code === 1) {
                roomIdEntry.setValue(response.roomId);
                passwordEntry.setValue(passKey);
                inputForm.setEnterRoom();
                submitBtn.click();
              }
            });
          }
        } else {
          let roomNum = roomIdEntry.getValue();
          let passKey = passwordEntry.getValue();
          if (!roomNum) {
            roomIdEntry.setErrorMessage("Cannot be empty!");
            roomIdEntry.setValue("");
          }
          if (!passKey) {
            passwordEntry.setErrorMessage("Cannot be empty!");
            passwordEntry.setValue("");
          }
          if (!roomNum || !passKey) {
            submitBtn.disabled = false;
            return;
          }
          request.send({
            action: "enter-room",
            username: username.innerText,
            roomId: roomNum,
            password: passKey
          }).then(response => {
            submitBtn.disabled = false;
            if (response.code === -1) {
              roomIdEntry.setErrorMessage("The room doesn't exist!");
            } else if (response.code === 0) {
              passwordEntry.setErrorMessage("Incorrect password!");
            } else if (response.code === 1) {
              localStorage.wordleRoomNum = roomNum;
              localStorage.wordlePassKey = passKey;
              sessionStorage.wordleUserId = response.users[response.users.length-1].id;
              enterRoom(response);
              overlay.dismiss();
            }
          });
        }
      };
    });
    overlay.show = () => {
      overlay.style.display = "flex";
    };
    overlay.dismiss = () => {
      overlay.style.display = "";
    }
    overlay.onclick = overlay.dismiss;
    overlay.append(inputForm);
    hall.append(overlay);
  });
  var room = create("div", {id: "room", hidden: ""});
  var roomHead = create("div", {id: "roomHead"}, roomHead => {
    var roominfo = create("div", {id: "roominfo"}, roominfo => {
      var roomNumber = create("div", {id: "roomNumber"});
      var roomKey = create("div", {id: "roomKey"});
      roomHead.setRoomNum = value => roomNumber.innerText = value;
      roomHead.setPassKey = value => roomKey.innerText = value;
      roominfo.append(roomNumber, roomKey);
      roomHead.append(roominfo);
    });
    var nickname = create("div", {id: "nickname"}, nickname => {
      roomHead.setNickname = value => nickname.innerText = value;
      roomHead.append(nickname);
    });
    var menuBtn = create("div", {id: "menuBtn"}, menuBtn => {
      menuBtn.onclick = event => {
        if (menuBarWrapper.hidden) {
          menuBarWrapper.hidden = false;
        } else {
          menuBarWrapper.hidden = true;
        }
      };
      roomHead.append(menuBtn);
    });
    room.append(roomHead);
  });
  var roomBody = create("div", {id: "roomBody"}, roomBody => {
    var group = create("div", {id: "group"}, group => {
      var groupBody = create("div", {id: "groupBody"});
      groupBody.createMember = function createMember(person) {
        var member = create("div", {id: "m" + person.id, class: "member"});
        var memberName = create("div", {class: "memberName"}, memberName => {
          member.append(memberName);
        });
        memberName.innerText = person.username;
        var memberState = create("div", {class: "memberState"}, memberState => {
          for (let i = 0; i < 6; i++) {
            let stateRow = create("div", {class: "stateRow"});
            for (let i = 0; i < 5; i++) {
              stateRow.append(create("div", {class: "statePoint"}));
            }
            memberState.append(stateRow);
          }
          member.append(memberState);
        });
        return member;
      };
      groupBody.refreshMember = function refreshMember(person) {
        var member = this.querySelector("#m" + person.id);
        var state = person.state;
        var statePoints = member.querySelectorAll(".statePoint");
        var stateColor = "";

        for (let i = 0; i < state.length; i += 5) {
          let word = state.slice(i, i + 5);
          if (word[0] == "." || word[0] == "_") {
            stateColor += word[0];
            break;
          }
          for (let color of container.compareWord(word)) {
            if (color === "yellow") {
              stateColor += "Y";
            } else if (color === "green") {
              stateColor += "G";
            } else {
              stateColor += "B";
            }
          }
        }

        for (let i = 0; i < stateColor.length; i++) {
          let ch = stateColor[i];
          if (ch === "G") {
            statePoints[i].classList.add("green");
            statePoints[i].innerText = state[i];
          } else if (ch === "Y") {
            statePoints[i].classList.add("yellow");
            statePoints[i].innerText = state[i];
          } else if (ch === ".") {
            if (i > 4 && stateColor.slice(i - 5, i) === "GGGGG") {
            } else member.classList.add("give-up");
            break;
          } else if (ch === "_") {
            member.classList.add("setter");
            break;
          } else {
            statePoints[i].classList.add("gray");
            statePoints[i].innerText = state[i];
          }
        }
      };
      groupBody.clear = function clear() {
        groupBody.querySelectorAll(".statePoint").forEach(statePoint => {
          statePoint.className = "statePoint";
          statePoint.innerText = "";
        });
        groupBody.querySelectorAll(".member.give-up").forEach(member => {
          member.classList.remove("give-up");
        });
        groupBody.querySelectorAll(".member.setter").forEach(member => {
          member.classList.remove("setter");
        });
      };
      group.append(groupBody);
      roomBody.append(group);
    });
    var container = create("div", {id: "container"}, container => {
      for (let i = 0; i < 6; i++) {
        let row = create("div", {class: "row"});
        for (let i = 0; i < 5; i++) {
          row.append(create("div", {class: "cell"}));
        }
        container.append(row);
      }
      container.onclick = event => {
        var cell = event.target;
        var currentRow = getCurrentRow();
        if (cell.matches(".cell") && currentRow && currentRow.contains(cell)) {
          cell.setAttribute("tabindex", "-1");
          cell.focus();
          cell.onblur = event => {
            cell.removeAttribute("tabindex")
            cell.onblur = null;
          };
        }
      };
      container.clear = () => {
        giveUpBtn.classList.remove("answered");
        giveUpBtn.innerText = "Give Up";
        container.classList.remove("give-up");
        container.classList.remove("setter");
        group.classList.remove("watcher");
        menuBar.closeBtn.click();
        for (let cell of container.querySelectorAll(".cell")) {
          cell.innerText = "";
          cell.className = "cell";
        }
        for (let key of keyboard.querySelectorAll(".gray, .yellow, .green")) {
          key.className = "key";
        }
        currentRowIndex = 0;
        currentCell = undefined;
      };
      container.isFinished = () => currentRowIndex >= rows.length;
      container.disable = () => currentRowIndex = rows.length;
      roomBody.append(container);
    });
    container.giveUp = answer => {
      giveUpBtn.classList.add("answered");
      giveUpBtn.innerText = answer;
      currentRowIndex = rows.length;
      currentCell = undefined;
      container.classList.add("give-up");
      group.classList.add("watcher");
    };
    var keyboardWrapper = create("div", {id: "keyboardWrapper"}, keyboardWrapper => {
      roomBody.append(keyboardWrapper);
    });
    var keyboard = create("div", {id: "keyboard"}, keyboard => {
      create("div", {}, div => {
        var row = ["Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"];
        for (let letter of row) {
          let key = create("div", {id: "lt"+letter, class: "key"});
          key.innerText = letter;
          div.append(key);
        }
        keyboard.append(div);
      });
      create("div", {}, div => {
        var row = ["A", "S", "D", "F", "G", "H", "J", "K", "L"];
        for (let letter of row) {
          let key = create("div", {id: "lt"+letter, class: "key"});
          key.innerText = letter;
          div.append(key);
        }
        keyboard.append(div);
      });
      create("div", {}, div => {
        var row = ["Z", "X", "C", "V", "B", "N", "M"];
        create("div", {id: "ltBACKSPACE", class: "key backspace"}, key => {
          key.innerHTML = `
          <svg aria-hidden="true" xmlns="http://www.w3.org/2000/svg"
            height="20" viewBox="0 0 24 24" width="20" class="game-icon"
            data-testid="icon-backspace">
            <path fill="var(--color-tone-1)"
              d="M22 3H7c-.69 0-1.23.35-1.59.88L0 12l5.41 8.11c.36.53.9.89
              1.59.89h15c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H7.07L2.4
              12l4.66-7H22v14zm-11.59-2L14 13.41 17.59 17 19 15.59 15.41
              12 19 8.41 17.59 7 14 10.59 10.41 7 9 8.41 12.59 12 9 15.59z">
            </path>
          </svg>`;
          div.append(key);
        });
        for (let letter of row) {
          let key = create("div", {id: "lt"+letter, class: "key"});
          key.innerText = letter;
          div.append(key);
        }
        create("div", {id: "ltENTER", class: "key enter"}, key => {
          key.innerText = "Enter";
          div.append(key);
        });
        keyboard.append(div);
      });
      keyboard.onpointerdown = event => {
        let keyElem = event.target.closest(".key");
        if (keyElem) {
          keyElem.setPointerCapture(event.pointerId);
          keyElem.classList.add("active");
          keyElem.onlostpointercapture = event => {
            keyElem.classList.remove("active");
            keyElem.onlostpointercapture = null;
          };
        }
        return false;
      };
      keyboard.onclick = event => input(event.target);
      keyboardWrapper.append(keyboard);
    });
    var rows = container.children;
    var currentRowIndex = 0;
    var currentCell;
    var answer;
    var isInputing = false;
    function getCurrentRow() {
      return rows[currentRowIndex];
    }
    function findCell(func) {
      if (!getCurrentRow()) return null;
      for (let cell of getCurrentRow().children) if (func(cell)) return cell;
      return null;
    }
    function findLastCell(func) {
      if (!getCurrentRow()) return null;
      var cells = getCurrentRow().children;
      for (let i = cells.length - 1; i > -1; i--) if (func(cells[i])) return cells[i];
      return null;
    }
    function bounceCurrentRow() {
      var currentRow = getCurrentRow();
      currentRow.classList.add("bounce");
      currentRow.onanimationend = event => {
        currentRow.classList.remove("bounce");
        currentRow.onanimationend = null;
      };
    }
    function compare(answer, word) {
      var colorArr = [];
      var answerArr = answer.split("");
      var wordArr = word.toUpperCase().split("");
      for (let i = 0; i < wordArr.length; i++) {
        if (wordArr[i] === answerArr[i]) {
          wordArr[i] = "";
          answerArr[i] = "";
          colorArr[i] = "green";
        }
      }
      for (let i = 0; i < wordArr.length; i++) {
        if (!wordArr[i]) continue;
        let index = answerArr.indexOf(wordArr[i]);
        if (index === -1) {
          colorArr[i] = "gray";
        } else {
          answerArr.splice(index, 1);
          colorArr[i] = "yellow";
        }
      }
      return colorArr;
    }
    function input(keyElem) {
      if (isInputing) return;
      let backspaceKey = !!keyElem.closest(".key.backspace");
      let enterKey = keyElem.matches(".key.enter");
      let currentRow = getCurrentRow();
      if (!currentRow) return;
      if (enterKey) {
        let word = currentRow.innerText.replaceAll(" ", "").toUpperCase();
        if (word.length < 5) {
          bounceCurrentRow();
          return;
        } else {
          container.inputWord(word);
        }
      } else if (backspaceKey) {
        let nextCell = currentRow.querySelector("[tabindex]");
        if (!nextCell) {
          nextCell = currentCell;
          if (nextCell) {
            if (!nextCell.innerText) while (nextCell = nextCell.previousElementSibling) if (nextCell.innerText) break;
          }
          if (!nextCell) nextCell = findLastCell(cell => cell.innerText);
        }
        if (nextCell) {
          nextCell.innerText = "";
          nextCell.classList.remove("popup");
          currentCell = nextCell;
        } else currentCell = undefined;
      } else if (keyElem.matches(".key")) {
        let nextCell = currentRow.querySelector("[tabindex]");
        if (!nextCell) {
          nextCell = currentCell;
          if (nextCell) {
            if (nextCell.innerText) while (nextCell = nextCell.nextElementSibling) if (!nextCell.innerText) break;
          }
          if (!nextCell) nextCell = findCell(cell => !cell.innerText);
        }
        if (nextCell) {
          let letter = keyElem.innerText;
          nextCell.innerText = letter;
          if (nextCell.classList.contains("popup")) {
            nextCell.classList.remove("popup");
            requestAnimationFrame(() => nextCell.classList.add("popup"));
          } else nextCell.classList.add("popup");
          currentCell = nextCell;
        } else currentCell = undefined;
      }
      container.querySelector(".cell[tabindex]")?.blur();
    }
    container.inputWord = async (word, useAnimation=true) => {
      isInputing = true;
      const response = await request.send({
        action: "input-word",
        userId: sessionStorage.wordleUserId,
        inputWord: word
      });
      if (response.code === 1) {
        sessionStorage.wordleUserState = response.user.state;
        sessionStorage.wordleRoomAnswer = answer;
        let colorArr = compare(answer, word);
        let currentRow = getCurrentRow();
        for (let i = 0; i < currentRow.children.length; i++) {
          let cell = currentRow.children[i];
          cell.innerText = word[i];
          let color = colorArr.shift();
          if (useAnimation) await new Promise((resolve, reject) => {
            cell.classList.add("flipover");
            cell.onanimationend = event => {
              cell.classList.remove("flipover");
              cell.classList.remove("popup");
              cell.classList.add(color);
              cell.classList.add("flipback");
              cell.onanimationend = event => {
                cell.classList.remove("flipback");
                cell.onanimationend = null;
                resolve();
              };
            };
          });
          else cell.classList.add(color);
          let keyElem = window["lt"+cell.innerText];
          if (keyElem.classList.contains("green")) {
          } else if (keyElem.classList.contains("yellow")) {
            if (color === "green") {
              keyElem.classList.remove("yellow");
              keyElem.classList.add(color);
            }
          } else if (keyElem.classList.contains("gray")) {
            if (color !== "gray") {
              keyElem.classList.remove("gray");
              keyElem.classList.add(color);
            }
          } else {
            keyElem.classList.add(color);
          }
        }
        if (answer === word) {
          group.classList.add("watcher");
          giveUpBtn.innerText = answer;
          giveUpBtn.classList.add("answered");
        } else if (currentRowIndex == rows.length - 1) {
          group.classList.add("watcher");
          giveUpBtn.innerText = answer;
          giveUpBtn.classList.add("answered");
        }
        if (answer === word) {
          currentRowIndex = rows.length;
        } else {
          ++currentRowIndex;
        }
        currentCell = undefined;
      } else {
        bounceCurrentRow();
      }
      isInputing = false;
    };
    container.setWord = word => {
      answer = word;
    };
    container.compareWord = word => {
      return compare(answer, word);
    };
    window.addEventListener("keydown", event => {
      if (room.hidden) return;
      if (username.getAttribute("contenteditable")) return;
      var key = event.key.toUpperCase();
      let keyElem = window["lt"+key];
      if (!keyElem) return;
      keyElem.classList.add("active");
    });
    window.addEventListener("keyup", event => {
      if (!keyboard.querySelector(".key.active")) return;
      var key = event.key.toUpperCase();
      var keyElem = window["lt"+key];
      if (!keyElem) return;
      keyElem.classList.remove("active");
      input(keyElem);
    });
    window.onblur = event => {
      for (let keyElem of keyboard.querySelectorAll(".active")) {
        keyElem.classList.remove("active");
      }
    };
    room.append(roomBody);
  });
  var roomFoot = create("div", {id: "roomFoot"}, roomFoot => {
    room.append(roomFoot);
  });
  var menuBarWrapper = create("div", {id: "menuBarWrapper", hidden: ""}, menuBarWrapper => {
    menuBarWrapper.onclick = event => {
      menuBarWrapper.hidden = true;
    };
    var menuBar = create("div", {id: "menuBar"}, menuBar => {
      var closeBtn = create("div", {class: "closeBtn"}, closeBtn => {
        closeBtn.onclick = () => {
          menuBarWrapper.hidden = true;
        };
        menuBar.closeBtn = closeBtn;
        menuBar.append(closeBtn);
      });
      var patchRect = create("div", {id: "patchRect"}, patchRect => {
        menuBar.append(patchRect);
      });
      var setWordBtn = create("div", {id: "setWordBtn"}, setWordBtn => {
        setWordBtn.innerText = "Set Word";
        setWordBtn.onclick = function setWord() {
          if (!container.isFinished()) {
            alert("To set a word please finish or give the current puzzle first.");
            return;
          }
          var word;
          while (true) {
            word = prompt("Set the word to guess:");
            if (word === null) return;
            word = word.trim();
            if (!word) return;
            if (word.length != 5) {
              alert(`The word: ${word} must consists of exactly 5 letters.`);
              continue;
            }
            if (!word.match(/\b\w{5}\b/)) {
              alert(`The word: ${word} cannot contain non-letter character.`);
              continue;
            }
            break;
          }
          request.send({
            action: "reset-word",
            inputWord: word,
            userId: sessionStorage.wordleUserId
          }).then(response => {
            if (response.code == 1) {
              container.clear();
              groupBody.clear();
              container.classList.add("setter");
              group.classList.add("watcher");
              container.disable();
              container.setWord(response.answer);
              giveUpBtn.innerText = word.toUpperCase();
              giveUpBtn.classList.add("answered");
              sessionStorage.wordleUserState = response.user.state;
              sessionStorage.wordleRoomAnswer = response.answer;
            } else if (response.code == -1) {
              alert(`The word: ${word} is not a valid word.`);
              setWord();
              closeBtn.click();
            } else {
              alert("There are players who have not finished the puzzle.");
              closeBtn.click();
            }
          });
        };
      });
      var randomBtn = create("div", {id: "randomBtn"}, randomBtn => {
        randomBtn.innerText = "Random";
        randomBtn.onclick = event => {
          if (!container.isFinished()) {
            alert("To set a word please finish or give the current puzzle first.");
            return;
          }
          request.send({
            action: "reset-word",
            userId: sessionStorage.wordleUserId
          }).then(response => {
            if (response.code) {
              container.clear();
              groupBody.clear();
              container.setWord(response.answer);
            } else {
              alert("There are players who are still playing now.")
            }
            closeBtn.click();
          });
        };
      });
      var giveUpBtn = create("div", {id: "giveUpBtn"}, giveUpBtn => {
        giveUpBtn.innerText = "Give Up";
        giveUpBtn.onclick = event => {
          if (!giveUpBtn.classList.contains("answered")) {
            request.send({
              action: "give-up",
              userId: sessionStorage.wordleUserId
            }).then(response => {
              sessionStorage.wordleUserState = response.user.state;
              sessionStorage.wordleRoomAnswer = response.answer;
              container.giveUp(response.answer);
            });
          } else {
            closeBtn.click();
          }
        };
      });
      var exitBtn = create("div", {id: "exitBtn"}, exitBtn => {
        exitBtn.innerText = "Exit Room";
        exitBtn.onclick = event => {
          request.send({
            action: "leave-room",
            userId: sessionStorage.wordleUserId
          }).then(response => {
            delete sessionStorage.wordleUserId;
            exitRoom();
          });
          closeBtn.click();
        };
      });
      var menuBarBody = create("div", {id: "menuBarBody"}, menuBarBody => {
        menuBarBody.append(setWordBtn, randomBtn, giveUpBtn, exitBtn);
        menuBar.append(menuBarBody);
      });
      menuBar.onclick = event => {
        event.stopPropagation();
      };
      menuBarWrapper.append(menuBar);
    });
    room.append(menuBarWrapper);
  });
  function enterRoom(message) {
    hall.hidden = true;
    room.hidden = false;
    roomHead.setRoomNum(localStorage.wordleRoomNum);
    roomHead.setPassKey(localStorage.wordlePassKey);
    roomHead.setNickname(localStorage.wordleUserName);
    container.setWord(message.answer);
    refresh(message.users);
    if (message.answer === sessionStorage.wordleRoomAnswer) {
      let state = sessionStorage.wordleUserState;
      (async () => {
        for (let i = 0; i < state.length; i += 5) {
          let word = state.slice(i, i + 5);
          if (word[0] == ".") {
            giveUpBtn.click();
            break;
          }
          if (word[0] == "_") {
            break;
          }
          await container.inputWord(word, false);
        }
      })();
    }
  }
  function refresh(people) {
    var members = groupBody.children;
    people = people.filter(person => person.id != sessionStorage.wordleUserId);
    for (let i = 0, membersToRemove = [];; i++) {
      let person = people[i], member = members[i];
      if (i < members.length && i < people.length) {
        if (member.id != "m" + person.id) {
          member.before(groupBody.createMember(person));
          groupBody.refreshMember(person);
        }
      } else if (i < members.length) {
        membersToRemove.push(member);
      } else if (i < people.length) {
        groupBody.append(groupBody.createMember(person));
        groupBody.refreshMember(person);
      } else {
        for (let member of membersToRemove) member.remove();
        break;
      }
    }
  }
  room.refresh = refresh;
  function exitRoom() {
    hall.hidden = false;
    room.hidden = true;
    container.clear();
  }
  sessionStorage.wordleUserId && request.send({
    action: "enter-room",
    username: localStorage.wordleUserName,
    roomId: localStorage.wordleRoomNum ?? "-1",
    password: localStorage.wordlePassKey
  }).then(response => {
    if (response.code === 1) {
      sessionStorage.wordleUserId = response.users[response.users.length-1].id;
      enterRoom(response);
    }
  });
  pager.append(hall, room);
  document.body.append(pager);
});