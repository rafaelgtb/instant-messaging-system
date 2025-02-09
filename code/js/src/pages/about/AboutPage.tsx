import * as React from "react";
import "../CSS/AboutPage.css";

const developers = [
  {
    name: "Diogo Ribeiro",
    id: "47207",
    github: "https://github.com/DiogoRibeiro47207",
  },
  {
    name: "António Coelho",
    id: "47236",
    github: "https://github.com/AntonioCoelho01",
  },
  {
    name: "Rafael Pegacho",
    id: "49423",
    github: "https://github.com/rafaeldeez",
  },
];

const AboutPage = () => {
  return (
    <main className="about-container">
      <section>
        <h1 className="about-title">About Instant Messaging System</h1>
        <p className="about-description">
          Welcome to the Instant Messaging System! This platform was developed
          to enable seamless communication and collaboration. Here’s more about
          it:
        </p>
      </section>

      <section className="about-details">
        <div className="about-details">
          <h2>Developed By:</h2>
          <ul>
            {developers.map((dev) => (
              <li key={dev.id}>
                <a href={dev.github} target="_blank" rel="noopener noreferrer">
                  {dev.name} - {dev.id}
                </a>
              </li>
            ))}
          </ul>
        </div>

        <div className="about-details">
          <h2>Repository:</h2>
          <p>
            <a
              href="https://github.com/isel-leic-daw/2024-daw-leic53d-g02-53d"
              target="_blank"
              rel="noopener noreferrer"
            >
              Instant Messaging System GitHub Repository
            </a>
          </p>
        </div>

        <div className="about-details">
          <h2>Institution:</h2>
          <p>
            <a
              href="https://www.isel.pt/"
              target="_blank"
              rel="noopener noreferrer"
            >
              Lisbon School of Engineering
            </a>
          </p>
        </div>
      </section>
    </main>
  );
};

export default AboutPage;
