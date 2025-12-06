class EmailDTO(object):
    def __init__(self, subject='', sender='', to_email='', date='', text=''):
        self.subject = subject
        self.sender = sender
        self.to_email = to_email
        self.date = date
        self.text = text

    def __str__(self):
        return (
            f"From: {self.sender}\n"
            f"To: {self.to_email}\n"
            f"Subject: {self.subject}\n"
            f"Date: {self.date}\n"
            f"Body:\n{self.text}"
        )

    def __repr__(self):
        return f"EmailDTO(subject={self.subject!r}, sender={self.sender!r}, to_email={self.to_email!r}, date={self.date!r}, text=...)"

